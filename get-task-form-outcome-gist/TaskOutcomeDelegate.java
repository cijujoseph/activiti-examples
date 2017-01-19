package com.activiti.extension.bean;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.activiti.service.runtime.SubmittedFormService;

@Component("taskOutcomeDelegate")
public class TaskOutcomeDelegate implements JavaDelegate {

	@Autowired
	private SubmittedFormService submittedFormService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		//task-id is the user-task id in the below task query!
		for (Task task : execution.getEngineServices().getTaskService().createTaskQuery()
				.processInstanceId(execution.getProcessInstanceId()).taskDefinitionKey("task-id").list()) {
			System.out.println(submittedFormService.getTaskSubmittedForm(task.getId()).getForm().getSelectedOutcome());
		}
	}

}
