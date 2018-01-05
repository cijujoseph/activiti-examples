package com.activiti.extension.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/*
 * A configuration class which will help get the custom packages scanned
 * 
 */

@Configuration
@PropertySource(value="classpath:email-listener.properties",ignoreResourceNotFound=true)
public class EmailExtensionConfiguration{
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}