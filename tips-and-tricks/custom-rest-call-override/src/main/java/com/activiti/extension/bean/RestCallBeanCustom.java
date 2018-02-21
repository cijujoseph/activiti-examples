package com.activiti.extension.bean;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestCallBeanCustom implements JavaDelegate{

    private static final Logger logger = LoggerFactory.getLogger(RestCallBeanCustom.class);

    protected static final String EXPRESSION_BASE_ENDPOINT = "baseEndpoint";
    protected static final String EXPRESSION_BASE_ENDPOINT_NAME = "baseEndpointName";
    protected static final String EXPRESSION_REST_URL = "restUrl";
    protected static final String EXPRESSION_HTTP_METHOD = "httpMethod";
    protected static final String EXPRESSION_REQUEST_MAPPING_JSON_TEMPLATE = "requestMappingJSONTemplate";

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        logger.info("now in my custom");
        
    }    

}