package com.alfresco.activiti.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/*
 * This is a configuration class to scan the Enterprise APIs in activiti-engine
 * library that will enable the enterprise clustering, metrics and admin capabilities. 
 */

@Configuration
@ComponentScan("com.activiti")
public class AppConfig {

}
