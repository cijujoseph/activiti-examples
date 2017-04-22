package com.activiti.extension.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.activiti.extension.rest.service.ESBServiceHTTPClient;
import com.codahale.metrics.annotation.Timed;


@RestController
@RequestMapping(value = "/rest/bpm")
public class BpmRestProxyResource {
	
    private final Logger logger = LoggerFactory.getLogger(BpmRestProxyResource.class);
    
    private final String MIME_TYPE_APPLICATION_JSON = "application/json";
	
    @Autowired
    private ESBServiceHTTPClient esbServiceHTTPClient;

    //A proxy layer for GET calls from activiti-app UI -> Activiti Server -> Secure Downstream APIs available in ESB layer
    @Timed
	@RequestMapping(value = "/esb/**", method = RequestMethod.GET, produces = MIME_TYPE_APPLICATION_JSON)
	public String getESBResource(HttpServletRequest request) throws IOException, RuntimeException {
    	return esbServiceHTTPClient.execute(request);
	}
}
