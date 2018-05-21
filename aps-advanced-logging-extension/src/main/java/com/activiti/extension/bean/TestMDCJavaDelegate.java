package com.activiti.extension.bean;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("testMDCJavaDelegate")
public class TestMDCJavaDelegate implements JavaDelegate {


    private static final Logger log = LoggerFactory
            .getLogger(TestMDCJavaDelegate.class);


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("logging to check MDC info in the logged line");
    }
}
