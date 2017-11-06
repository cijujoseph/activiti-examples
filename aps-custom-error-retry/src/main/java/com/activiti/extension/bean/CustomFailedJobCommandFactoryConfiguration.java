package com.activiti.extension.bean;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.api.engine.ProcessEngineConfigurationConfigurer;

//Register this custom failed job command factory with the process engine.

@Component
public class CustomFailedJobCommandFactoryConfiguration implements ProcessEngineConfigurationConfigurer {
	
	@Autowired
	CustomFailedJobCommandFactory customFailedJobCommandFactory;

	@Override
	public void processEngineConfigurationInitialized(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
		
		springProcessEngineConfiguration.setFailedJobCommandFactory(customFailedJobCommandFactory);
	}

}
