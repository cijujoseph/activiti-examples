package com.alfresco.model;

public class DynamicSubProcessModel {
	
    private String processDefinitionKey;

    private String executionId;

    private String activityId;

    private String processInstanceId;

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	
}