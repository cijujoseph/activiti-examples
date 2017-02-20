package com.alfresco;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
	
	private static final Logger log = LoggerFactory
			.getLogger(HttpClient.class);
	public static final ContentType APPLICATION_JSON_CONTENT = ContentType.APPLICATION_JSON;	
	private static final Integer DEFAULT_CONNECT_TIMEOUT_MS = 20000;
	private static final Integer DEFAULT_SOCKET_TIMEOUT_MS = 60000;
	
	public String handleResponse(
	            final HttpResponse response) throws IOException {	    	
	    	StatusLine statusLine = response.getStatusLine();	    	
	    	HttpEntity entity = response.getEntity();
	    	String responseContent = null;
	    	if (entity != null) {
	    		responseContent = IOUtils.toString(response.getEntity().getContent());	    		
	    	}
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(),
						statusLine.getReasonPhrase());
			}
			return responseContent;
	 }

	public Object execute(String endpointurl, String method, String requestBody,
			ContentType contentType, Map<String, Object> headers,
			Boolean handleResponse, Integer connectTimeout, Integer socketTimeout) throws IOException,RuntimeException {
		String url = null;
		Executor executor = null;
		
		url = endpointurl;
		executor = Executor.newInstance();
		
		HttpResponse httpResponse;
		Object response = null;		
		switch (method) {
			case "GET":
				httpResponse = executor.execute(Request.Get(url).connectTimeout(connectTimeout).socketTimeout(socketTimeout)).returnResponse();
				response = handleResponse ? handleResponse(httpResponse): httpResponse;
				
				break;

			case "POST":
				httpResponse = executor.execute(Request.Post(url).connectTimeout(connectTimeout).socketTimeout(socketTimeout).bodyString(requestBody, contentType)).returnResponse();
				response = handleResponse ? handleResponse(httpResponse): httpResponse;
				
				break;

			case "PUT":							
				httpResponse = executor.execute(Request.Put(url).connectTimeout(connectTimeout).socketTimeout(socketTimeout).bodyString(requestBody, contentType)).returnResponse();
				response = handleResponse ? handleResponse(httpResponse): httpResponse;
				
				break;

			default:
				throw new RuntimeException("Runtime Error while invoking HTTP Endpoint - Invalid Method");
			}	
		
		return response;

	}

	//Use default timeout configured
	public Object execute(String endpointurl, String method, String body,
			ContentType contentType, Map<String, Object> headers,Boolean handleResponse) throws IOException,RuntimeException {
		return execute(endpointurl, method, body, contentType, headers, handleResponse, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_SOCKET_TIMEOUT_MS);
	}
}
