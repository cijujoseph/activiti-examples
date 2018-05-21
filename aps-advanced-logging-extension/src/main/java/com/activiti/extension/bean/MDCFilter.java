package com.activiti.extension.bean;

import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//Refer http://www.baeldung.com/mdc-in-log4j-2-logback for a good tutorial on Mapped Diagnostic Context (MDC)

@Component("mdcFilter")
public class MDCFilter extends OncePerRequestFilter {

    @Autowired
    UserService userService;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            String user =  null;

            if(SecurityUtils.getCurrentUserId()!=null){
                user = userService.findUser(SecurityUtils.getCurrentUserId()).getFullName();
            }

            MDC.put("user", user);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDC.clear();
        }
    }
}
