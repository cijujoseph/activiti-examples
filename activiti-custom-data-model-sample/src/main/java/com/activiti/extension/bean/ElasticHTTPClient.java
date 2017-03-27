package com.activiti.extension.bean;

import java.io.IOException;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
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

// http client bean with connection pool
@Component("elasticHTTPClient")
public class ElasticHTTPClient {

	protected static final Logger logger = LoggerFactory.getLogger(ElasticHTTPClient.class);
	private final CloseableHttpClient httpClient;

	public ElasticHTTPClient() {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(200);
		cm.setMaxTotal(200);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}

	public String execute(String restEndpointURI, String requestJson, String method) {

		String jsonString = null;
		CloseableHttpResponse response = null;
		HttpRequestBase httpRequest;
		if(method.equals("POST")){
			httpRequest = new HttpPost(restEndpointURI);
		}
		else if(method.equals("PUT")){
			httpRequest = new HttpPut(restEndpointURI);
		}
		else{
			httpRequest = new HttpGet(restEndpointURI);
		}
		try {
			if(method.equals("POST") || method.equals("PUT")){
				StringEntity input = new StringEntity(requestJson, "UTF-8");
				System.out.println(requestJson);
				input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				input.setContentEncoding("UTF-8");
				if(method.equals("POST")) {
					((HttpPost) httpRequest).setEntity(input);
				}
				else {
					((HttpPut) httpRequest).setEntity(input);
				}
			}

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
				logger.error("Failed to close HTTP response after invoking: "+  method + " "+ restEndpointURI, e);
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
			throw new ActivitiException("error while executing http request: " + method + " "+ httpRequest.getURI(), e);
		}

		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new ActivitiException("error while executing http request " + method + " "+ httpRequest.getURI()
					+ " with status code: " + response.getStatusLine().getStatusCode());
		}
		return response;
	}

}
