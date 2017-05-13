package com.activiti.extension.bean;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.User;
import com.activiti.service.api.UserService;
import com.activiti.service.runtime.ActivitiService;

@Component
public class ListenToSQS {

	@Autowired
	ActivitiService activitiService;

	@Autowired
	RepositoryService repositoryService;
	
	@Autowired
	UserService userService;

	@MessageMapping("aps-inbound")
	public void onRandomQMessage(String payload) {
		User user = userService.findActiveUserByEmail("admin@app.activiti.com");
		Authentication.setAuthenticatedUserId(Long.toString(user.getId()));
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey("sqs-start")
				.latestVersion().singleResult();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("initiator", user.getId());
		map.put("message", payload);
		activitiService.startProcessInstance (pd.getId(), map, "sqs-start");
	}
}