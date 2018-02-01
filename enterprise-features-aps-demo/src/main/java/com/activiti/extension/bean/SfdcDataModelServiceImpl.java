package com.activiti.extension.bean;

import com.activiti.api.datamodel.AlfrescoCustomDataModelService;
import com.activiti.model.editor.datamodel.DataModelAttributeRepresentation;
import com.activiti.model.editor.datamodel.DataModelDefinitionRepresentation;
import com.activiti.model.editor.datamodel.DataModelEntityRepresentation;
import com.activiti.runtime.activiti.bean.datamodel.AttributeMappingWrapper;
import com.activiti.variable.VariableEntityWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SfdcDataModelServiceImpl implements AlfrescoCustomDataModelService {

	private static Logger logger = LoggerFactory.getLogger(SfdcDataModelServiceImpl.class);

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private SfdcRestClient sfdcRestClient;

	@Override
	public ObjectNode getMappedValue(DataModelEntityRepresentation entityDefinition, String fieldName,
			Object fieldValue) {

		String sObject = entityDefinition.getName();

		StringBuffer sbr = null;
		for (DataModelAttributeRepresentation attribute : entityDefinition.getAttributes()) {
			if (sbr!=null) {
				sbr.append(",").append(attribute.getName());
			} else {
				sbr = new StringBuffer();
				sbr.append(attribute.getName());
				
			}
		}
		String entityFields = sbr.toString();

		try {

			JsonNode responseNode = sfdcRestClient.selectSingle(entityFields, sObject, fieldName, (String) fieldValue);
			return (ObjectNode) objectMapper.readTree(objectMapper.writeValueAsString(responseNode));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public VariableEntityWrapper getVariableEntity(String keyValue, String variableName, String processDefinitionId,
			DataModelEntityRepresentation entityDefinition) {

		if (keyValue != null) {
			String sObject = entityDefinition.getName();

			StringBuffer sbr = null;
			for (DataModelAttributeRepresentation attribute : entityDefinition.getAttributes()) {
				if (sbr!=null) {
					sbr.append(",").append(attribute.getName());
				} else {
					sbr = new StringBuffer();
					sbr.append(attribute.getName());
					
				}
			}

			String entityFields = sbr.toString();

			try {

				JsonNode responseNode = sfdcRestClient.selectSingle(entityFields, sObject, "Id", keyValue);
				ObjectNode dataNode = (ObjectNode) objectMapper.readTree(objectMapper.writeValueAsString(responseNode));

				VariableEntityWrapper variableEntityWrapper = new VariableEntityWrapper(variableName,
						processDefinitionId, entityDefinition);
				variableEntityWrapper.setEntity(dataNode);
				variableEntityWrapper.setKey(keyValue);
				return variableEntityWrapper;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String storeEntity(List<AttributeMappingWrapper> attributeDefinitionsAndValues,
			DataModelEntityRepresentation entityDefinition, DataModelDefinitionRepresentation dataModel) {

		String idAttribute = "Id";
		String idValue = null;
		// Set up a map of all the column names and values
		Map<String, Object> parameters = new HashMap<String, Object>();
		for (AttributeMappingWrapper attributeMappingWrapper : attributeDefinitionsAndValues) {
			logger.info(attributeMappingWrapper.getAttribute().getName());
			logger.info(attributeMappingWrapper.getValue().toString());
			
			if (attributeMappingWrapper.getAttribute().getName().equals(idAttribute)
					&& attributeMappingWrapper.getValue() != null) {
				idValue = (String) attributeMappingWrapper.getValue();
			} else {
				parameters.put(attributeMappingWrapper.getAttribute().getName(), attributeMappingWrapper.getValue());
			}
		}

		try {
			if (idValue != null) {
				sfdcRestClient.update(parameters, idValue, entityDefinition.getName());
				return idValue;
			} else {
				JsonNode responseNode = sfdcRestClient.create(parameters, entityDefinition.getName());
				return responseNode.get("id").textValue();
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}