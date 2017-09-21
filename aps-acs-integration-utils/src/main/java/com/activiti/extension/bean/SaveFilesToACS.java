package com.activiti.extension.bean;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.domain.runtime.RelatedContent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;

@Component("saveFilesToACS")
public class SaveFilesToACS implements JavaDelegate {

	@Autowired
	ACSHTTPClient acsHTTPClient;

	@Autowired
	protected ObjectMapper objectMapper;
	
	@Autowired
	ContentUtils contentUtils;

	private Expression acsRelativePath;

	private Expression contentField;

	String ROOT_URL = "/alfresco/api/-default-/public/alfresco/versions/1";

	public void execute(DelegateExecution execution) throws JsonParseException, JsonMappingException, IOException{

		// Find Node ID
		String getNodeUrl = ROOT_URL + "/nodes/-root-" + "?relativePath=" + URLEncoder.encode(getExpressionValue(execution, acsRelativePath), "UTF-8");
		String response = acsHTTPClient.execute(getNodeUrl, null, "GET", null);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
		});
		String nodeId = (String) ((Map<String, Object>) responseMap.get("entry")).get("id");

		//Upload content
		List<RelatedContent> relatedContentList = null;
		if(contentField!=null){
			relatedContentList = contentUtils.getFieldContent(execution.getProcessInstanceId(), contentField.getExpressionText());
			
		} else {
			relatedContentList = contentUtils.getContents(execution.getProcessInstanceId());
		}		
		
		if(relatedContentList!=null) {
			for (RelatedContent relatedContent : relatedContentList) {	
				String uploadNodeUrl = ROOT_URL + "/nodes/" + nodeId + "/children";
				String uploadResponse = acsHTTPClient.execute(uploadNodeUrl, null, "POST", relatedContent);
			}
		}
	}

	private String getExpressionValue(DelegateExecution execution, Expression field) {
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		Expression expression = expressionManager.createExpression(field.getExpressionText());
		return expression.getValue(execution).toString();
	}

}
