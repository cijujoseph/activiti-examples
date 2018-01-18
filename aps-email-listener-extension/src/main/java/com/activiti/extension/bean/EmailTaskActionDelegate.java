package com.activiti.extension.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.User;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;

@Component("emailTaskActionDelegate")
public class EmailTaskActionDelegate implements JavaDelegate {
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RuntimeService runtimeService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String subject = (String) execution.getVariable("subject");
		String from = (String) execution.getVariable("from");
		
		try {
			Pattern taskIdPattern = Pattern.compile(execution.getVariable("taskIdRegex").toString());
			Matcher taskIdMatcher = taskIdPattern.matcher(subject);
			System.out.println(subject);
			System.out.println( execution.getVariable("taskIdRegex"));
			String taskId = null;
			String action = null;
			String outcome = null;
			
			if (taskIdMatcher.find()) {
				taskId = taskIdMatcher.group(1);
			} else {
				System.out.println("no task id match!");
				throw new BpmnError("Error");
			}
			
			Pattern taskActionPattern = Pattern.compile(execution.getVariable("taskActionRegex").toString());
			Matcher taskActionMatcher = taskActionPattern.matcher(subject);

			if (taskActionMatcher.find()) {
				action = taskActionMatcher.group(1);
			}
			
			Pattern taskOutcomePattern = Pattern.compile(execution.getVariable("taskOutcomeRegex").toString());
			Matcher taskOutcomeMatcher = taskOutcomePattern.matcher(subject);

			if (taskOutcomeMatcher.find()) {
				outcome = taskOutcomeMatcher.group(1);
			}
			
			if(action.equals("Complete")){
				User user = userService.findActiveUserByEmail(from);
				Task currentTask = taskService.createTaskQuery().taskId(taskId).taskTenantId("tenant_1").singleResult();
				if(currentTask.getAssignee().equals(Long.toString(user.getId()))){
					Authentication.setAuthenticatedUserId(Long.toString(user.getId()));
					SecurityUtils.assumeUser(user);
					if(outcome!=null){
						Map map = new HashMap();
						System.out.println("outcome var - form"+currentTask.getFormKey()+"outcome");
						map.put("form"+currentTask.getFormKey()+"outcome", outcome);
						taskService.complete(taskId, map);
					} else {
						taskService.complete(taskId);
					}
					
				} else {
					System.out.println("not the correct assignee!");
					throw new BpmnError("Error");
				}
			} else {
				System.out.println("action not supported!");
				throw new BpmnError("Error");
			}
			
		} catch (Exception e){
			System.out.println(e.getStackTrace());
			throw new BpmnError("Error");
		}

	}

}
