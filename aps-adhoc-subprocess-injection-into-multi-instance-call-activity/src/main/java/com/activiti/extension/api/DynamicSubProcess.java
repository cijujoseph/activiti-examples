package com.activiti.extension.api;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alfresco.model.DynamicSubProcessModel;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.runtime.Execution;

@RestController
public class DynamicSubProcess {

	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	ProcessEngineConfigurationImpl processEngineConfiguration;

	ProcessEngine pe;

	@RequestMapping(value = "/enterprise/dynamicsubprocess", method = RequestMethod.POST, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	public void injectNewSubProcess(@RequestBody DynamicSubProcessModel request) throws Exception {
		CallActivityBehavior call = new CallActivityBehavior(request.getProcessDefinitionKey(), null);
		Execution execution = null;
		if (request.getExecutionId() != null && !request.getExecutionId().equals("")) {
			execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(request.getExecutionId())
					.singleResult();
		} else {
			List<Execution> execList = runtimeService.createExecutionQuery()
					.processInstanceId(request.getProcessInstanceId()).list();
			for (Execution currentExecution : execList) {
				if (currentExecution.getParentId() != null && currentExecution.getParentId().equals(request.getProcessInstanceId())
						&& currentExecution.getActivityId() != null
						&& currentExecution.getActivityId().equals(request.getActivityId())) {
					execution = currentExecution;
					break;
				}
			}
		}
		ExecutionEntity executionEntity = (ExecutionEntity) execution;
		

		CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
		commandExecutor.execute(new Command<ExecutionEntity>() {
			public ExecutionEntity execute(CommandContext commandContext) {
				String executionId = executionEntity.getId();
				int instanceCount = (int) runtimeService.getVariable(executionId, "nrOfInstances");
				runtimeService.setVariable(executionId, "nrOfActiveInstances",
						(int) runtimeService.getVariable(executionId, "nrOfActiveInstances") + 1);
				runtimeService.setVariable(executionId, "nrOfInstances", instanceCount + 1);

				ActivityExecution activityExecution = executionEntity.createExecution();
				activityExecution.setActive(true);
				activityExecution.setConcurrent(true);
				activityExecution.setScope(false);
				runtimeService.setVariableLocal(activityExecution.getId(), "loopCounter", instanceCount);

				try {
					call.execute(activityExecution);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return executionEntity;
			}
		});
	}

}
