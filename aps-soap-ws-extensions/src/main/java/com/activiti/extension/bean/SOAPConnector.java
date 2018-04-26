package com.activiti.extension.bean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.service.api.EndpointService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.activiti.engine.delegate.Expression;
import org.w3c.dom.Document;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Component("soapConnector")
public class SOAPConnector implements JavaDelegate {

	protected static final String ENDPOINT_URL = "endpointUrl";
	protected static final String SOAP_ACTION = "soapAction";
	protected static final String SOAP12 = "isSoap12";
	protected static final String REQUEST_TEMPLATE = "soapRequestTemplate";
	protected static final String ENDPOINTCONFIG_NAME = "sharedEndpointConfigurationName";
	protected static final String RESPONSE_BODYVARIABLE = "soapResponseBody";
	protected static final String RESPONSE_VARIABLE_MAPPING = "soapResponseVariableMapping";

	@Autowired
	protected Configuration freeMarkerConfig;

	@Autowired
	private EndpointService endpointService;

	@Autowired
	private SOAPHTTPClient soapHTTPClient;
	
	protected static final Logger logger = LoggerFactory.getLogger(SOAPConnector.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		Expression isSoap12 = DelegateHelper.getFieldExpression(execution, SOAP12);
		Expression soapRequestTemplate = DelegateHelper.getFieldExpression(execution, REQUEST_TEMPLATE);
		Expression soapAction = DelegateHelper.getFieldExpression(execution, SOAP_ACTION);
		Expression endpointUrl = DelegateHelper.getFieldExpression(execution, ENDPOINT_URL);
		Expression endpointName = DelegateHelper.getFieldExpression(execution, ENDPOINTCONFIG_NAME);
		Expression soapResponseBody = DelegateHelper.getFieldExpression(execution, RESPONSE_BODYVARIABLE);
		Expression soapResponseVariableMapping = DelegateHelper.getFieldExpression(execution, RESPONSE_VARIABLE_MAPPING);

		Template requestRemplate = new Template("soapRequest",
				new StringReader(soapRequestTemplate.getExpressionText()), freeMarkerConfig);

		String request = FreeMarkerTemplateUtils.processTemplateIntoString(requestRemplate, execution.getVariables());
		logger.info(request);

		Boolean isSoap12Boolean = false;
		if (isSoap12 != null) {
			isSoap12Boolean = Boolean.parseBoolean(isSoap12.getExpressionText());
		}
		String webServiceEndpointURL = null;
		String encodedAuth = null;
		if (endpointName != null && endpointName.getExpressionText() != null) {

			EndpointConfiguration endpointConfig = endpointService.getConfigurationByName(endpointName.getExpressionText());
			
			webServiceEndpointURL = endpointConfig.getUrl();
			if(endpointConfig.getBasicAuth()!=null){
				String username = endpointConfig.getBasicAuth().getUsername();
				String password = endpointService
						.getDecryptedBasicAuthPassword(endpointConfig.getBasicAuth().getPassword());
				
				encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
			}
			//If using Java 7 use the below logic
			//encodedAuth = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
		} else {
			webServiceEndpointURL = getExpressionValue(execution, endpointUrl);
		}
		String response = soapHTTPClient.execute(request, webServiceEndpointURL, soapAction.getExpressionText(),
				isSoap12Boolean, encodedAuth);
		if (soapResponseBody != null && soapResponseBody.getExpressionText() != null) {
			execution.setVariable(soapResponseBody.getExpressionText(), response);
		}
		
		String processedXmlResponse = removeNameSpace(response);
		DocumentBuilderFactory builderFactory =
		        DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
		    builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
		    e.printStackTrace();  
		}
		logger.info("processedXmlResponse: " + processedXmlResponse);
		Document xmlResponseDocument = builder.parse(new ByteArrayInputStream(processedXmlResponse.getBytes()));
		XPath xPath =  XPathFactory.newInstance().newXPath();
		if (soapResponseVariableMapping != null && soapResponseVariableMapping.getExpressionText() != null) {
			List<String> responseVariableList = Arrays
					.asList(soapResponseVariableMapping.getExpressionText().split("\\s*,\\s*"));
			for (String responseVariable : responseVariableList) {
				String[] parts = responseVariable.split(":");
				String variableValue = xPath.compile("/Envelope/Body"+getExpressionValue(execution, parts[1])).evaluate(xmlResponseDocument);
				execution.setVariable(parts[0], variableValue);
			}
		}

		logger.info("Response from delegate" + response);
	}

	private String getExpressionValue(DelegateExecution execution, Expression field) {
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		Expression expression = expressionManager.createExpression(field.getExpressionText());
		return expression.getValue(execution).toString();
	}
	
	private String getExpressionValue(DelegateExecution execution, String expressionText) {
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		Expression expression = expressionManager.createExpression(expressionText);
		return expression.getValue(execution).toString();
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
