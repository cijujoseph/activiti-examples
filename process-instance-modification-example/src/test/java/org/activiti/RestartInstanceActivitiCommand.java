package org.activiti;

import java.util.Map;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

public class RestartInstanceActivitiCommand implements Command<Void> {

	private final String executionId;
	private final String activityId;
	private final Map<String, Object> variables;

	public RestartInstanceActivitiCommand(String executionId, String activityId, Map<String, Object> variables) {
		this.executionId = executionId;
		this.activityId = activityId;
		this.variables = variables;
	}

	public Void execute(CommandContext commandContext) {
		ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(this.executionId);
		execution.setActivity(new ActivityImpl(this.activityId, execution.getProcessDefinition()));
		ActivityImpl activity = execution.getProcessDefinition().findActivity(activityId);
		if (variables != null) {
			execution.setVariables(variables);
		}
		System.out.println("--re-trying--");
		execution.executeActivity(activity);
		return null;
	}

}
