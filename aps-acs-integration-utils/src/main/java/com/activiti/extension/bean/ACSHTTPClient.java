package com.activiti.extension.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import org.activiti.engine.ActivitiException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.api.EndpointService;
import com.activiti.service.runtime.RelatedContentStreamProvider;

// http client bean with connection pool
@Component("acsHTTPClient")
public class ACSHTTPClient {

	protected static final Logger logger = LoggerFactory.getLogger(ACSHTTPClient.class);
	private final CloseableHttpClient httpClient;

	@Autowired
	protected Environment env;

	@Autowired
	private EndpointService endpointService;

	@Autowired
	private RelatedContentStreamProvider relatedContentStreamProvider;

	public ACSHTTPClient() {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(200);
		cm.setMaxTotal(200);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}

	public String execute(String requestURI, String requestJson, String method, RelatedContent relatedContent)
			throws UnsupportedEncodingException {

		EndpointConfiguration endpointConfig = null;

		// Loop through all the endpoints configured in the Identity Management
		// module of activiti-app and find the ACS endpoint config
		List<EndpointConfiguration> endpoints = endpointService.getConfigurationsForTenant(1L);
		for (EndpointConfiguration endpoint : endpoints) {
			if (endpoint.getName().equalsIgnoreCase(env.getProperty("acs.endpoint.name", "ACS Core API")))
				endpointConfig = endpoint;
		}

		String acsEndpointURL = endpointConfig.getUrl() + requestURI;

		String jsonString = null;
		CloseableHttpResponse response = null;
		HttpRequestBase httpRequest;
		if (method.equals("POST")) {
			httpRequest = new HttpPost(acsEndpointURL);
		} else if (method.equals("PUT")) {
			httpRequest = new HttpPut(acsEndpointURL);
		} else {
			httpRequest = new HttpGet(acsEndpointURL);
		}
		try {
			if (relatedContent != null) {
					try {
						InputStream inputStream = relatedContentStreamProvider.getContentStream(relatedContent);

						byte[] streamByteArray = IOUtils.toByteArray(inputStream);

						MultipartEntityBuilder builder = MultipartEntityBuilder.create();
						builder.addBinaryBody("filedata", streamByteArray, ContentType.APPLICATION_OCTET_STREAM,
								relatedContent.getName());

						String fileName = relatedContent.getName();
						builder.addTextBody("filename", fileName);

						HttpEntity multipart = builder.build();
						((HttpPost) httpRequest).setEntity(multipart);

					} catch (Exception e) {
						throw new ActivitiException("error while performing content extraction" + e.getMessage());
					}
				
			} else if (method.equals("POST") || method.equals("PUT")) {
				StringEntity input = new StringEntity(requestJson, "UTF-8");
				System.out.println(requestJson);
				input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				input.setContentEncoding("UTF-8");
				if (method.equals("POST")) {
					((HttpPost) httpRequest).setEntity(input);
				} else {
					((HttpPut) httpRequest).setEntity(input);
				}
			}
			String username = endpointConfig.getBasicAuth().getUsername();
			String password = endpointService
					.getDecryptedBasicAuthPassword(endpointConfig.getBasicAuth().getPassword());
			//If using Java 8 use the default
			//String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
			String encoding = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
			httpRequest.addHeader("Authorization", "Basic " + encoding);

			Long httpStartTime = new Date().getTime();
			response = (CloseableHttpResponse) executeHttpRequest(httpRequest, method);
			logger.debug("Total HTTP Execution Time: " + ((new Date()).getTime() - httpStartTime));

			try {
				jsonString = EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
				logger.error("error while parsing JSON response: " + jsonString, e);
			}

		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error("Failed to close HTTP response after invoking: " + method + " " + acsEndpointURL, e);
			}

			if (httpRequest != null) {
				httpRequest.releaseConnection();
			} else {
				logger.debug("Could not release connection.");
			}
		}

		return jsonString;
	}

	protected HttpResponse executeHttpRequest(HttpRequestBase httpRequest, String method) {

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpRequest);
		} catch (IOException e) {
			throw new ActivitiException("error while executing http request: " + method + " " + httpRequest.getURI(),
					e);
		}

		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new ActivitiException("error while executing http request " + method + " " + httpRequest.getURI()
					+ " with status code: " + response.getStatusLine().getStatusCode());
		}
		return response;
	}

}
