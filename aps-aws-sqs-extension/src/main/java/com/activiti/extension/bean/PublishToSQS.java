package com.activiti.extension.bean;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Component;

@Component("publishToSQS")
public class PublishToSQS implements JavaDelegate {

	@Autowired
	private QueueMessagingTemplate sqsMsgTemplate;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		this.sqsMsgTemplate.convertAndSend("aps-outbound",
				"from activiti, for process with id: " + execution.getProcessInstanceId());

	}

}
