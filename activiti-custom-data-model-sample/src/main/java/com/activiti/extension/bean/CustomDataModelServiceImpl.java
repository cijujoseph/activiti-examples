package com.activiti.extension.bean;

import com.activiti.api.datamodel.AlfrescoCustomDataModelService;
import com.activiti.model.editor.datamodel.DataModelDefinitionRepresentation;
import com.activiti.model.editor.datamodel.DataModelEntityRepresentation;
import com.activiti.runtime.activiti.bean.datamodel.AttributeMappingWrapper;
import com.activiti.variable.VariableEntityWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomDataModelServiceImpl implements AlfrescoCustomDataModelService {

	private static Logger logger = LoggerFactory.getLogger(CustomDataModelServiceImpl.class);

	private static final String POLICY_ENTITY_NAME = "Policy";
	private static final String CLAIM_ENTITY_NAME = "Claim";
	
	private static final String ELASTIC_BASE = "http://127.0.0.1:9200/";

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private ElasticHTTPClient elasticHTTPClient;

	@Override
	public ObjectNode getMappedValue(DataModelEntityRepresentation entityDefinition, String fieldName,
			Object fieldValue) {
		
		String esUrl;
		if (StringUtils.equals(entityDefinition.getName(), POLICY_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/policyevent/";
		} else if (StringUtils.equals(entityDefinition.getName(), CLAIM_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/claimevent/";
		} else{
			return null;
		}

		String esResponse = elasticHTTPClient
				.execute(esUrl + (String) fieldValue, null, "GET");
		JsonNode responseNode;
		try {
			
			responseNode = objectMapper.readTree(esResponse);
			if (responseNode.findValue("found").asText().equals("true")) {
				ObjectNode policyNode = (ObjectNode) objectMapper
						.readTree(objectMapper.writeValueAsString(responseNode.get("_source")));
				return policyNode;
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public VariableEntityWrapper getVariableEntity(String keyValue, String variableName, String processDefinitionId,
			DataModelEntityRepresentation entityValue) {
		

		String esUrl;
		if (StringUtils.equals(entityValue.getName(), POLICY_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/policyevent/";
		} else if (StringUtils.equals(entityValue.getName(), CLAIM_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/claimevent/";
		} else{
			return null;
		}
		if (keyValue != null) {
			String esResponse = elasticHTTPClient
					.execute(esUrl + (String)keyValue, null, "GET");
			JsonNode responseNode;
			try {
				
				responseNode = objectMapper.readTree(esResponse);
				if (responseNode.findValue("found").asText().equals("true")) {
					ObjectNode policyNode = (ObjectNode) objectMapper
							.readTree(objectMapper.writeValueAsString(responseNode.get("_source")));
					VariableEntityWrapper variableEntityWrapper = new VariableEntityWrapper(variableName,
							processDefinitionId, entityValue);
					variableEntityWrapper.setEntity(policyNode);
					variableEntityWrapper.setKey(keyValue);	
					return variableEntityWrapper;
				}
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String storeEntity(List<AttributeMappingWrapper> attributeDefinitionsAndValues,
			DataModelEntityRepresentation entityDefinition, DataModelDefinitionRepresentation dataModel) {
		logger.info("create policy start");
		String esUrl;
		String idAttribute;
		if (StringUtils.equals(entityDefinition.getName(), POLICY_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/policyevent/";
			idAttribute = "policyId";
		} else if (StringUtils.equals(entityDefinition.getName(), CLAIM_ENTITY_NAME)) {
			esUrl = ELASTIC_BASE+"insuranceindex/claimevent/";
			idAttribute = "claimId";
		} else{
			return null;
		}
		// Set up a map of all the column names and values
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (AttributeMappingWrapper attributeMappingWrapper : attributeDefinitionsAndValues) {
			System.out.println(attributeMappingWrapper.getAttribute().getName());
			System.out.println(attributeMappingWrapper.getValue());
			parameters.put(attributeMappingWrapper.getAttribute().getName(), attributeMappingWrapper.getValue());
		}
		try {
			String jsonString = new ObjectMapper().writeValueAsString(parameters);
			String response = elasticHTTPClient.execute(
					esUrl + (String) parameters.get(idAttribute),
					jsonString, "POST");
			logger.info(response);
			return (String) parameters.get(idAttribute);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}