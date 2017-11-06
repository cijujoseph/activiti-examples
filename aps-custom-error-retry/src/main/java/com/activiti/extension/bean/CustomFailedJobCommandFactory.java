package com.activiti.extension.bean;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomFailedJobCommandFactory implements FailedJobCommandFactory {

	@Override
	public Command<Object> getCommand(String jobId, Throwable exception) {
	    return new CustomJobRetryCmd(jobId, exception);
	}

}
