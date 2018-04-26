package com.activiti.extension.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.extension.bean.SOAPHTTPClient;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.EndpointService;
import com.activiti.service.editor.AlfrescoFormService;
import com.activiti.service.runtime.FormStoreService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.core.env.Environment;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@RestController
public class RESTProxyForSOAP {

	protected static final Logger logger = LoggerFactory.getLogger(RESTProxyForSOAP.class);

	@Autowired
	protected AlfrescoFormService formService;

	@Autowired
	protected Configuration freeMarkerConfig;

	@Autowired
	private EndpointService endpointService;

	@Autowired
	private SOAPHTTPClient soapHTTPClient;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
    protected FormStoreService formStoreService;

	@RequestMapping(value = "/enterprise/soapproxy", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@Timed
	public List<Map<String, String>> getListFromSOAP(@RequestParam(value = "formId", required = false) Long formId, 
			@RequestParam(value = "runtimeFormId", required = false) String runtimeFormId,
			@RequestParam(value = "fieldId", required = false) String fieldId,
			@RequestParam Map<String, String> allRequestParams)
			throws XPathExpressionException, SAXException, IOException, TemplateException {
		logger.info(allRequestParams.toString());
		FormFieldRepresentation field = null;
		if(runtimeFormId!=null && !runtimeFormId.equals("")) {
			field = objectMapper.readValue(formStoreService.getForm(runtimeFormId).getDefinition(), FormDefinitionRepresentation.class).allFieldsAsMap().get(fieldId);
		} else {
			field = formService.getForm(formId).getFormDefinition().allFieldsAsMap().get(fieldId);
		}
		Object customProperties = field.getParam("customProperties");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		if (customProperties != null) {
			String isSoap12Property = field.getType() + "-isSoap12";
			String endpointUrlProperty = field.getType() + "-endpointUrl";
			String soapActionProperty = field.getType() + "-soapAction";
			String soapRequestTemplateProperty = field.getType() + "-soapRequestTemplate";
			String sharedEndpointConfigurationNameProperty = field.getType() + "-sharedEndpointConfigurationName";
			String soapResponsePathMappingProperty = field.getType() + "-soapResponsePathMapping";

			Map<String, Object> customPropertiesMap = (Map<String, Object>) customProperties;
			String isSoap12 = (String) customPropertiesMap.get(isSoap12Property);
			String endpointUrl = (String) customPropertiesMap.get(endpointUrlProperty);
			String soapAction = (String) customPropertiesMap.get(soapActionProperty);
			String soapRequestTemplate = (String) customPropertiesMap.get(soapRequestTemplateProperty);
			String sharedEndpointConfigurationName = (String) customPropertiesMap
					.get(sharedEndpointConfigurationNameProperty);
			String soapResponsePathMapping = (String) customPropertiesMap.get(soapResponsePathMappingProperty);

			Template requestRemplate = new Template("soapRequest", new StringReader(soapRequestTemplate),
					freeMarkerConfig);

			String request = FreeMarkerTemplateUtils.processTemplateIntoString(requestRemplate, allRequestParams);
			logger.info(request);

			Boolean isSoap12Boolean = false;
			if (isSoap12 != null) {
				isSoap12Boolean = Boolean.parseBoolean(isSoap12);
			}
			String webServiceEndpointURL = null;
			String encodedAuth  = null;
			if (sharedEndpointConfigurationName != null && sharedEndpointConfigurationName != "") {
				EndpointConfiguration endpointConfig = endpointService.getConfigurationByName(sharedEndpointConfigurationName);
				
				webServiceEndpointURL = endpointConfig.getUrl();
				if(endpointConfig.getBasicAuth()!=null) {
					String username = endpointConfig.getBasicAuth().getUsername();
					String password = endpointService
							.getDecryptedBasicAuthPassword(endpointConfig.getBasicAuth().getPassword());
					
					encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
					//If using Java 7 use the below logic
					//encodedAuth = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
				}
			} else {
				webServiceEndpointURL = endpointUrl;
			}
			String response = soapHTTPClient.execute(request, webServiceEndpointURL, soapAction, isSoap12Boolean, encodedAuth);

			String processedXmlResponse = removeNameSpace(response);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try {
				builder = builderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			logger.info("processedXmlResponse: " + processedXmlResponse);
			Document xmlResponseDocument;

			xmlResponseDocument = builder.parse(new ByteArrayInputStream(processedXmlResponse.getBytes()));

			XPath xPath = XPathFactory.newInstance().newXPath();
			Map<String, String> configMap = new HashMap<String, String>();
			List<String> responseVariableList = Arrays.asList(soapResponsePathMapping.split("\\s*,\\s*"));
			for (String responseVariable : responseVariableList) {
				String[] parts = responseVariable.split(":");
				configMap.put(parts[0], parts[1]);
			}
			NodeList nodeList = (NodeList) xPath.compile("/Envelope/Body" + configMap.get("repeatingNodePath"))
					.evaluate(xmlResponseDocument, XPathConstants.NODESET);

			HashMap<String, String> map;
			for (int i = 0; i < nodeList.getLength(); i++) {
				map = new HashMap<String, String>();
				Node currentNode = nodeList.item(i);
				map.put("id", xPath.compile(configMap.get("id")).evaluate(currentNode));
				map.put("label", xPath.compile(configMap.get("label")).evaluate(currentNode));
				list.add(map);
			}

			logger.info("Response from controller" + response);
		}
		return list;
	}

	private String removeNameSpace(String processedXml) {
		String xmlResponse = "";
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Source xslt = new StreamSource(this.getClass().getResourceAsStream("/removeNs.xslt"));
			Transformer transformer = factory.newTransformer(xslt);

			Source text = new StreamSource(new StringReader(processedXml));
			StringWriter writer = new StringWriter();
			transformer.transform(text, new StreamResult(writer));
			xmlResponse = writer.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return xmlResponse;
	}

}