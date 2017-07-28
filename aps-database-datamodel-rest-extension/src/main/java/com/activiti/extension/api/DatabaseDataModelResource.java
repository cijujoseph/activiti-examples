package com.activiti.extension.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.idm.DataSource;
import com.activiti.model.editor.datamodel.DataModelAttributeRepresentation;
import com.activiti.model.editor.datamodel.DataModelDefinitionRepresentation;
import com.activiti.model.editor.datamodel.DataModelEntityRepresentation;
import com.activiti.service.datamodel.DataModelEntityDao;
import com.activiti.service.editor.AlfrescoDataModelService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.activiti.service.api.DataSourceService;

@RestController
public class DatabaseDataModelResource {

	protected static final Logger logger = LoggerFactory.getLogger(DatabaseDataModelResource.class);

	@Autowired
	protected AlfrescoDataModelService alfrescoDataModelService;

	@Autowired
	protected DataSourceService dataSourceService;

	@Autowired
	protected DataModelEntityDao dataModelEntityDao;

	@Autowired
	protected ObjectMapper objectMapper;

	@RequestMapping(value = "/enterprise/custom-api/datamodels/{modelId}/entities/{entityName}/{keyValue}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public ObjectNode lookupDataModel(@PathVariable Long modelId, @PathVariable String entityName,
			@PathVariable String keyValue, @RequestParam(value = "keyName", required = false) String keyName) {
		try {

			DataModelDefinitionRepresentation dm = alfrescoDataModelService.getDataModel(modelId)
					.getDataModelDefinition();
			DataModelEntityRepresentation entityDefinition = dm.findEntity(entityName);

			DataModelAttributeRepresentation lookupAttribute = null;
			for (DataModelAttributeRepresentation attribute : entityDefinition.getAttributes()) {
				if (attribute.getName().equals(keyName)) {
					lookupAttribute = attribute;
					break;
				}
			}

			DataSource dataSource = dataSourceService.getById(dm.getDataSourceId());
			if (lookupAttribute != null && StringUtils.isNotEmpty(lookupAttribute.getMappedName())
					&& dataSource != null) {

				//Since there is already a dataModelEntityDao.findOneBy() method, using it instead of writing duplicate code!
				ObjectNode valueNode = dataModelEntityDao.findOneBy(dataSource, entityDefinition,
						lookupAttribute.getMappedName(),
						lookupAttribute.isNumber() ? Long.valueOf(keyValue) : keyValue);
				logger.debug(valueNode.asText());
				return valueNode;

			}
			return null;

		} catch (Exception e) {
			logger.error("Error while doing datamodel lookup ", e);
			return null;
		}
	}

	@RequestMapping(value = "/enterprise/custom-api/datamodels/{modelId}/entities/{entityName}", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public List<Map<String, Object>> getAllEntries(@PathVariable Long modelId, @PathVariable String entityName) {

		try {

			DataModelDefinitionRepresentation dm = alfrescoDataModelService.getDataModel(modelId)
					.getDataModelDefinition();
			DataModelEntityRepresentation entityDefinition = dm.findEntity(entityName);

			DataSource dataSource = dataSourceService.getById(dm.getDataSourceId());
			if (dataSource != null) {
				JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
				List<Map<String, Object>> resultSet = jdbcTemplate
						.queryForList("select * from " + entityDefinition.getTableName());
				List<Map<String, Object>> entityList = mapResponse(resultSet, entityDefinition);
				return entityList;
			}
			return null;

		} catch (Exception e) {
			logger.error("Error retrieving data from jdbc data source", e);
			return null;

		}

	}

	private JdbcTemplate getJdbcTemplate(DataSource dataSource) throws IOException, JsonProcessingException {
		JsonNode configNode = objectMapper.readTree(dataSource.getConfig());
		String jdbcUrl = getJsonPropertyValue(DataSource.JDBC_URL_PROPERTY, configNode);
		String driverClass = getJsonPropertyValue(DataSource.DRIVER_CLASS_PROPERTY, configNode);
		String username = getJsonPropertyValue(DataSource.USERNAME_PROPERTY, configNode);
		String password = getJsonPropertyValue(DataSource.PASSWORD_PROPERTY, configNode);

		DriverManagerDataSource managedDataSource = new DriverManagerDataSource(jdbcUrl, username, password);
		managedDataSource.setDriverClassName(driverClass);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(managedDataSource);
		return jdbcTemplate;
	}

	private String getJsonPropertyValue(String name, JsonNode node) {
		String propValue = null;
		JsonNode propNode = node.get(name);
		if (propNode != null && propNode.isNull() == false) {
			propValue = propNode.asText();
		}
		return propValue;
	}

	private List<Map<String, Object>> mapResponse(List<Map<String, Object>> dbResultSet,
			DataModelEntityRepresentation entityDefinition) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : dbResultSet) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (DataModelAttributeRepresentation attributeDefinition : entityDefinition.getAttributes()) {
				map.put(attributeDefinition.getName(), row.get(attributeDefinition.getMappedName()));
			}
			result.add(map);
		}
		return result;
	}

}
