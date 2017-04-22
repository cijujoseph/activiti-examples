package com.activiti.extension.rest.service;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.EndpointService;
import com.activiti.service.exception.BadRequestException;

@Component("esbServiceHTTPClient")
public class ESBServiceHTTPClient {

	private final Logger logger = LoggerFactory.getLogger(ESBServiceHTTPClient.class);

	private final String MIME_TYPE_APPLICATION_JSON = "application/json";
	private final String CUSTOM_HTTP_HEADER_USER = "X-BPM-User";

	private final String CUSTOMER_HTTP_HEADER_USER_PROPERTY_NAME = "bpm.security.http-user-header";
	private final String ENDPOINT_ESB_PROPERTY_NAME = "bpm.security.esb-endpoint-name";
	private final String TENANT_ID_PROPERTY_NAME = "bpm.security.tenantId";

	private final String ENDPOINT_ESB_DEFAULT = "ESB";
	private final String TENANT_ID_DEFAULT = "1";

	@Autowired
	private EndpointService endpointService;

	@Autowired
	private Environment env;

	public String execute(HttpServletRequest request) throws IOException, RuntimeException {

		HttpClient httpClient = null;
		EndpointConfiguration endpointConfig = null;

		//Loop through all the endpoints configured in the Identity Management module of activiti-app and find the ESB endpoint config
		List<EndpointConfiguration> endpoints = endpointService.getConfigurationsForTenant(
				Long.getLong(env.getProperty(TENANT_ID_PROPERTY_NAME, TENANT_ID_DEFAULT), 1L));
		for (EndpointConfiguration endpoint : endpoints) {
			if (endpoint.getName().equalsIgnoreCase(env.getProperty(ENDPOINT_ESB_PROPERTY_NAME, ENDPOINT_ESB_DEFAULT)))
				endpointConfig = endpoint;
		}

		if (endpointConfig == null)
			throw new BadRequestException("Cannot find ESB endpoint");

		String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String uri = new AntPathMatcher().extractPathWithinPattern(pattern, path);
		String query = request.getQueryString();
		String url = endpointConfig.getUrl() + "/" + uri;
		if (query != null && !query.isEmpty())
			url += "?" + query;

		logger.debug("Endpoint URL: " + url);

		//Get the username/password, decrypt the password and set the basic auth header in the downstream API call.
		String username = endpointConfig.getBasicAuth().getUsername();
		String password = endpointService.getDecryptedBasicAuthPassword(endpointConfig.getBasicAuth().getPassword());
		// create client with basic auth
		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		} else {
			throw new BadRequestException("Endpoint credentials not set");
		}

		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(HttpHeaders.ACCEPT, MIME_TYPE_APPLICATION_JSON);
		
		// Add the current logged-in userId into the header so that the
		// downstream system can track the users who are invoking these APIs and
		// detect any suspicious actions!
		httpGet.addHeader(env.getProperty(CUSTOMER_HTTP_HEADER_USER_PROPERTY_NAME, CUSTOM_HTTP_HEADER_USER),
				SecurityUtils.getCurrentUserObject().getExternalId());
		//Allowing only GET calls. Make the request and respond back to the caller
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() >= 200
				&& response.getStatusLine().getStatusCode() < 300) {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} else {
			String message = "";
			if (response.getStatusLine() != null) {
				message = response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
			}
			throw new BadRequestException("Unexpected response received: " + message);
		}
	}
}
