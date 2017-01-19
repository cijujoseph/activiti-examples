package com.activiti.extension.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.dmn.engine.DmnRuleService;
import com.activiti.dmn.engine.RuleEngineExecutionResult;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RuleExecutionEnterpriseApi{
	/*
	 * This API is protected by basic authentication, because it is under
	 * com.activiti.extension.api package.Note that the endpoint needs to have
	 * /enterprise as first element in the url, as this is configured in the
	 * SecurityConfiguration to be protected with basic authentication. In this case
	 * http://localhost:8080/activiti-app/api/enterprise/dmn/{key}/evaluate
	 * 
	 */
	
	@Autowired
    protected DmnRuleService ruleService;
	
	 protected static final Logger logger = LoggerFactory.getLogger(RuleExecutionEnterpriseApi.class);
	    
    @RequestMapping(value = "/enterprise/dmn/{key}/evaluate",
            method = RequestMethod.POST,
            produces = "application/json",
            consumes = { "application/json" })
    @Timed
    public Map<String, Object> evaluate(@PathVariable("key") String key, @RequestBody Map<String, Object> requestMap) {
		
    	RuleEngineExecutionResult executionResult = ruleService.executeDecisionByKey(key,
    			requestMap);
    	return executionResult.getResultVariables();
    }
    
}
