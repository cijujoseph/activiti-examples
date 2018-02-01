package com.activiti.extension.bean;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("sfdcRestClient")
public class SfdcRestClient {
	
	protected static final Logger logger = LoggerFactory.getLogger(SfdcRestClient.class);

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	private Environment env;
	
	public static final String SFDC_USERNAME = "sfdc.username";
	public static final String SFDC_PW = "sfdc.password";
	public static final String SFDC_CLIENT_ID = "sfdc.clientId";
	public static final String SFDC_CLIENT_SECRET = "sfdc.clientSecret";
	public static final String SFDC_GRANT_TYPE = "sfdc.grantType";
	public static final String SFDC_OAUTH_TOKEN_URL = "sfdc.oauthTokenUrl";
	public static final String SFDC_BASE_URL = "sfdc.sfdcRestBaseUrl";
	public static final String SFDC_API_URI = "sfdc.sfdcApiUri";
	
	private String authorizationToken;

	public String getAuthorizationToken() {
		return this.authorizationToken;
	}

	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		RestTemplate template = builder.build();
		return template;
	}

	@PostConstruct
	public void setAccessToken() {

		HttpHeaders authReqHeaders = new HttpHeaders();
		authReqHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
		requestMap.add("client_id", env.getProperty(SFDC_CLIENT_ID));
		requestMap.add("client_secret", env.getProperty(SFDC_CLIENT_SECRET));
		requestMap.add("grant_type", env.getProperty(SFDC_GRANT_TYPE));
		requestMap.add("username", env.getProperty(SFDC_USERNAME));
		requestMap.add("password", env.getProperty(SFDC_PW));
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(requestMap,
				authReqHeaders);
		
		JsonNode tokenResponse = restTemplate.postForObject(env.getProperty(SFDC_OAUTH_TOKEN_URL), request, JsonNode.class);
		this.authorizationToken = tokenResponse.findValue("access_token").asText();
		logger.debug(this.authorizationToken);

	}

	public void update(Map<String, Object> requestBody, String Id, String sObject) throws JsonProcessingException {
		
		if(this.authorizationToken == null){
			setAccessToken();
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Authorization", "Bearer " + this.authorizationToken);
		requestHeaders.set("Content-Type", "application/json");
		requestHeaders.set("Accept", "application/json");

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(env.getProperty(SFDC_BASE_URL) + env.getProperty(SFDC_API_URI) + "/sobjects/" + sObject + "/" + Id)
				.queryParam("_HttpMethod", "PATCH");
		ObjectMapper objectMapper = new ObjectMapper();
		
		HttpEntity<String> request = new HttpEntity<String>(objectMapper.writeValueAsString(requestBody),
				requestHeaders);
		logger.info(objectMapper.writeValueAsString(requestBody));
		restTemplate.postForObject(uriBuilder.build().toUri(), request, JsonNode.class);
	}
	


	public JsonNode create(Map<String, Object> requestBody, String sObject) throws JsonProcessingException {
		
		if(this.authorizationToken == null){
			setAccessToken();
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Authorization", "Bearer " + this.authorizationToken);
		requestHeaders.set("Content-Type", "application/json");
		requestHeaders.set("Accept", "application/json");

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(env.getProperty(SFDC_BASE_URL) + env.getProperty(SFDC_API_URI) + "/sobjects/" + sObject + "/");
		ObjectMapper objectMapper = new ObjectMapper();
		HttpEntity<String> request = new HttpEntity<String>(objectMapper.writeValueAsString(requestBody),
				requestHeaders);
		logger.info(objectMapper.writeValueAsString(requestBody));
		return restTemplate.postForObject(uriBuilder.build().toUri(), request, JsonNode.class);
	}
	
	public JsonNode selectSingle(String entityFields, String sObject, String selector, String selectorValue) throws JsonProcessingException {
		
		if(this.authorizationToken == null){
			setAccessToken();
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Authorization", "Bearer " + this.authorizationToken);
		requestHeaders.set("Accept", "application/json");
		
		HttpEntity entity = new HttpEntity(requestHeaders);
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(env.getProperty(SFDC_BASE_URL) + env.getProperty(SFDC_API_URI)+ "/query/")
				.queryParam("q", "SELECT " + entityFields + " FROM " + sObject + " where " + selector + "='" +selectorValue+"' LIMIT 1");
		logger.info(uriBuilder.build().toUri().toString());
		JsonNode jsonResponse = restTemplate.exchange(uriBuilder.build().toUri(), HttpMethod.GET, entity, JsonNode.class).getBody() ;
		if(jsonResponse.get("totalSize").equals(0)){
			return null;
		} else {
			return jsonResponse.get("records").get(0);
		}
		 
		
	}
}