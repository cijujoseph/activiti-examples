package com.activiti.extension.bean;

import java.util.ArrayList;

import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.rules.RulesDeployer;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.stereotype.Component;

import com.activiti.api.engine.ProcessEngineConfigurationConfigurer;

@Component
public class CustomProcessEngineConfiguration implements ProcessEngineConfigurationConfigurer {

	@Override
	public void processEngineConfigurationInitialized(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
		
		Deployer ruleDeployer = new RulesDeployer();
        if(springProcessEngineConfiguration.getCustomPostDeployers()==null) {
        	springProcessEngineConfiguration.setCustomPostDeployers(new ArrayList<Deployer>());
        } 
        springProcessEngineConfiguration.getCustomPostDeployers().add(ruleDeployer);

	}

}
