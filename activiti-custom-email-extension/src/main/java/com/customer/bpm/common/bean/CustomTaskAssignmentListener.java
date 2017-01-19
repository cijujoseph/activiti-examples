package com.customer.bpm.common.bean;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.customer.bpm.common.bean.TaskUtils.*;

@Component("customTaskAssignmentListener")
public class CustomTaskAssignmentListener implements ActivitiEventListener {

	private static final Logger log = LoggerFactory.getLogger(CustomTaskAssignmentListener.class);

	@Autowired
	private TaskUtils taskUtils;

	@Autowired
	private ContentUtils contentUtils;

	@Autowired
	private EmailUtils emailUtils;

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		String executionId = event.getExecutionId();

		RuntimeService runtimeService = event.getEngineServices().getRuntimeService();
		String processInstanceId = event.getProcessInstanceId();
		if (event.getType().equals(ActivitiEventType.TASK_ASSIGNED)) {

			Map<String, Object> templateVariables = new HashMap<String, Object>();
			// Add all process variables
			templateVariables.putAll(runtimeService.getVariables(executionId));
			Task task = (Task) ((ActivitiEntityEvent) event).getEntity();
			// Add all task related variables.
			Map<String, Object> taskVariables = taskUtils.getTaskDetails(task);
			templateVariables.putAll(taskVariables);
			String template = null;
			String subject = "";
			switch (task.getTaskDefinitionKey()) {
			case "task-1":
				template = "custom-template-1.ftl";
				subject = "Subject 1";
				break;
			case "task-2":
				template = "custom-template-2.ftl";
				subject = "Subject 2";
				break;
			default:
				template = "default-template.ftl";
				subject = "Default Email";
				break;
			}
			String[] toList = { (String) taskVariables.get(TASK_ASSIGNEE_EMAIL_KEY) };
			try {
				// send email
				emailUtils.sendEmail(toList, subject, emailUtils.evaluateTemplate(template, templateVariables),
						contentUtils.getContents(processInstanceId));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Unable to send email");
			}
		}

	}

}
