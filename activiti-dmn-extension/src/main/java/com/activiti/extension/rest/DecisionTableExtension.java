package com.activiti.extension.rest;

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

@RestController
public class DecisionTableExtension{
	
	/*
	 * A class like this in the com.activiti.extension.rest package will be
	 * added to the rest endpoints for the application (e.g. for use in the UI),
	 * which use the cookie approach to determine the user. The url will be
	 * mapped under /app. So, if logged in into the UI of the BPM Suite, one
	 * could go to http://localhost:8080/activiti-app/app/rest/dmn/{key}/evaluate
	 * and see the result of the custom rest endpoint:
	 * 
	 * 
	 */
	
	@Autowired
    protected DmnRuleService ruleService;
	
	 protected static final Logger logger = LoggerFactory.getLogger(DecisionTableExtension.class);
	    
    @RequestMapping(value = "/rest/dmn/{key}/evaluate",
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
