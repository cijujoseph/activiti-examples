package com.activiti.extension.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.activiti.extension.bean.RestCallBeanCustom;

@Configuration
public class ExtensionConfiguration{
	
	@Autowired
    private ApplicationContext context;
	
	@Bean(name="activiti_restCallDelegate")
	public RestCallBeanCustom createRestBean() {
		BeanDefinitionRegistry factory = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
		factory.removeBeanDefinition("activiti_restCallDelegate");
		return new RestCallBeanCustom();
	}
}