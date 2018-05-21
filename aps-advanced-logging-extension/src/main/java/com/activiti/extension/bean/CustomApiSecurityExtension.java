package com.activiti.extension.bean;

import com.activiti.api.security.AlfrescoApiSecurityExtender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class CustomApiSecurityExtension implements AlfrescoApiSecurityExtender {

    @Autowired
    MDCFilter mdcFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
                http.addFilterAfter(
                mdcFilter, BasicAuthenticationFilter.class);

    }
}
