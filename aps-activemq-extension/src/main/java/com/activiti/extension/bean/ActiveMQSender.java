package com.activiti.extension.bean;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component("activeMQSender")
public class ActiveMQSender implements JavaDelegate {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		jmsTemplate.send(new ActiveMQQueue("aps-outbound"), new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				ObjectMessage objectMessage = session
						.createObjectMessage("from activiti, for process with id: " + execution.getProcessInstanceId());
				return objectMessage;
			}
		});

	}

}
