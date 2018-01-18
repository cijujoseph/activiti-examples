package com.activiti.extension.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.User;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;
import com.activiti.service.runtime.ActivitiService;

@Component("emailProcessStartDelegate")
public class EmailProcessStartDelegate implements JavaDelegate {

	@Autowired
	TaskService taskService;

	@Autowired
	UserService userService;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	ActivitiService activitiService;

	@Autowired
	RepositoryService repositoryService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		try {

			String from = (String) execution.getVariable("from");
			User user = userService.findActiveUserByEmail(from);
			Authentication.setAuthenticatedUserId(Long.toString(user.getId()));
			SecurityUtils.assumeUser(user);
			ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey(execution.getVariable("processDefKey").toString()).latestVersion()
					.singleResult();
			String subject = (String) execution.getVariable("subject");
			activitiService.startProcessInstance(pd.getId(), execution.getVariables(), "Email - " + subject);

		} catch (Exception e) {
			throw new BpmnError("Error");
		}

	}

}
