package com.activiti.extension.bean;

import com.activiti.api.datamodel.AlfrescoCustomDataModelService;
import com.activiti.model.editor.datamodel.DataModelDefinitionRepresentation;
import com.activiti.model.editor.datamodel.DataModelEntityRepresentation;
import com.activiti.runtime.activiti.bean.datamodel.AttributeMappingWrapper;
import com.activiti.variable.VariableEntityWrapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CustomDataModelServiceImpl implements AlfrescoCustomDataModelService {

	private static Logger logger = LoggerFactory.getLogger(CustomDataModelServiceImpl.class);

	@Autowired
	private DynamoDB dynamoDB;

	private static final String POLICY_ENTITY_NAME = "Policy";
	private static final String CLAIM_ENTITY_NAME = "Claim";

	private static final String POLICY_TABLE = "Policy";
	private static final String CLAIM_TABLE = "Claim";

	@Autowired
	protected ObjectMapper objectMapper;

	@Override
	public ObjectNode getMappedValue(DataModelEntityRepresentation entityDefinition, String fieldName,
			Object fieldValue) {

		Table table;
		String idAttribute;
		if (StringUtils.equals(entityDefinition.getName(), POLICY_ENTITY_NAME)) {
			table = dynamoDB.getTable(POLICY_TABLE);
			idAttribute = "policyId";
		} else if (StringUtils.equals(entityDefinition.getName(), CLAIM_ENTITY_NAME)) {
			table = dynamoDB.getTable(CLAIM_TABLE);
			idAttribute = "claimId";
		} else {
			return null;
		}

		Item item = table.getItem(idAttribute, fieldValue);
		if (item != null) {
			ObjectNode objectNode;
			try {
				objectNode = (ObjectNode) objectMapper.readTree(item.toJSON());
				return objectNode;
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
	public VariableEntityWrapper getVariableEntity(String keyValue, String variableName, String processDefinitionId,
			DataModelEntityRepresentation entityValue) {

		Table table;
		String idAttribute;
		if (StringUtils.equals(entityValue.getName(), POLICY_ENTITY_NAME)) {
			table = dynamoDB.getTable(POLICY_TABLE);
			idAttribute = "policyId";
		} else if (StringUtils.equals(entityValue.getName(), CLAIM_ENTITY_NAME)) {
			table = dynamoDB.getTable(CLAIM_TABLE);
			idAttribute = "claimId";
		} else {
			return null;
		}
		if (keyValue != null) {
			Item item = table.getItem(idAttribute, keyValue);
			if (item != null) {
				ObjectNode objectNode;
				try {
					objectNode = (ObjectNode) objectMapper.readTree(item.toJSON());
					VariableEntityWrapper variableEntityWrapper = new VariableEntityWrapper(variableName,
							processDefinitionId, entityValue);
					variableEntityWrapper.setEntity(objectNode);
					variableEntityWrapper.setKey(keyValue);
					return variableEntityWrapper;
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	public String storeEntity(List<AttributeMappingWrapper> attributeDefinitionsAndValues,
			DataModelEntityRepresentation entityDefinition, DataModelDefinitionRepresentation dataModel) {
		logger.info("store entity start");

		String idAttribute;
		Table table;
		if (StringUtils.equals(entityDefinition.getName(), POLICY_ENTITY_NAME)) {
			table = dynamoDB.getTable(POLICY_TABLE);
			idAttribute = "policyId";
		} else if (StringUtils.equals(entityDefinition.getName(), CLAIM_ENTITY_NAME)) {
			table = dynamoDB.getTable(CLAIM_TABLE);
			idAttribute = "claimId";
		} else {
			return null;
		}
		Item item = new Item();
		String keyValue = null;
		for (AttributeMappingWrapper attributeMappingWrapper : attributeDefinitionsAndValues) {
			if (attributeMappingWrapper.getAttribute().getName().equals(idAttribute)) {
				keyValue = (String) attributeMappingWrapper.getValue();
			}
			item.with(attributeMappingWrapper.getAttribute().getName(), attributeMappingWrapper.getValue());
		}
		table.putItem(item);
		return keyValue;
	}
}