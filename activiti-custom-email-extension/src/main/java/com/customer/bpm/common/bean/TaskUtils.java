package com.customer.bpm.common.bean;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.User;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;

@Component("taskUtils")
public class TaskUtils {

	private static final Logger log = LoggerFactory.getLogger(TaskUtils.class);
	
	public static final String TASK_DUE_DATE_KEY = "taskDueDate";
	public static final String TASK_START_DATE_KEY = "taskStartDate";
	public static final String TASK_NAME_KEY = "taskName";
	public static final String TASK_ID_KEY = "taskId";
	public static final String TASK_ASSIGNEE_NAME_KEY = "assigneeName";
	public static final String TASK_ASSIGNEE_EMAIL_KEY = "assigneeEmail";
	public static final String TASK_URL_KEY = "taskUrl";
	public static final String TASK_CREATOR_KEY = "taskCreator";
	public static final String BASE_URL_KEY = "homeUrl";

	private final String BPM_BASE_URL_PROPERTY_NAME = "bpm.baseUrl";
	private final String BPM_BASE_URL_DEFAULT = "http://localhost:8080/activiti-app";
	public static final String BPM_APPS_URI = "/workflow/#";

	@Autowired
	private UserService userService;

	@Autowired
	private Environment env;

	public Map<String, Object> getTaskDetails(Task task) {
		//To be used with an Event Listener

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		
		User user = SecurityUtils.getCurrentUserObject();
		String taskCreator = "";
		if(user!=null){
			taskCreator = user.getFullName();
		}
		taskVariables.put(TASK_CREATOR_KEY, taskCreator);
		taskVariables.put(TASK_NAME_KEY, task.getName());
		System.out.println(task.getAssignee());
		User assignee = userService.findUser(new Long(task.getAssignee()));
		taskVariables.put(TASK_ASSIGNEE_NAME_KEY, assignee.getFullName());
		taskVariables.put(TASK_ASSIGNEE_EMAIL_KEY, assignee.getEmail());
		taskVariables.put(TASK_ID_KEY, task.getId());
		taskVariables.put(TASK_DUE_DATE_KEY, task.getDueDate());
		taskVariables.put(TASK_START_DATE_KEY, task.getCreateTime());
		taskVariables.put(TASK_URL_KEY, getTaskUrl(task.getId()));
		taskVariables.put(BASE_URL_KEY, env.getProperty(BPM_BASE_URL_PROPERTY_NAME, BPM_BASE_URL_DEFAULT));
		return taskVariables;

	}
	
	public Map<String, Object> getTaskDetails(DelegateTask task) {
		
		//To be used with a Task Listener

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		
		User user = SecurityUtils.getCurrentUserObject();
		String taskCreator = "";
		if(user!=null){
			taskCreator = user.getFullName();
		}
		taskVariables.put(TASK_CREATOR_KEY, taskCreator);
		taskVariables.put(TASK_NAME_KEY, task.getName());
		User assignee = userService.findUser(new Long(task.getAssignee()));
		taskVariables.put(TASK_ASSIGNEE_NAME_KEY, assignee.getFullName());
		taskVariables.put(TASK_ASSIGNEE_EMAIL_KEY, assignee.getEmail());
		taskVariables.put(TASK_ID_KEY, task.getId());
		taskVariables.put(TASK_DUE_DATE_KEY, task.getDueDate());
		taskVariables.put(TASK_START_DATE_KEY, task.getCreateTime());
		taskVariables.put(TASK_URL_KEY, getTaskUrl(task.getId()));
		taskVariables.put(BASE_URL_KEY, env.getProperty(BPM_BASE_URL_PROPERTY_NAME, BPM_BASE_URL_DEFAULT));
		return taskVariables;

	}

	public String getTaskUrl(String taskId) {

		String taskUrlSuffixif = (taskId != null) ? "/task/" + taskId : "/tasks";
		String taskUrl = env.getProperty(BPM_BASE_URL_PROPERTY_NAME, BPM_BASE_URL_DEFAULT) + BPM_APPS_URI
				+ taskUrlSuffixif;
		return taskUrl;
	}

}
