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

// soap http client bean with connection pool
@Component("soapHTTPClient")
public class SOAPHTTPClient {

	protected static final Logger logger = LoggerFactory.getLogger(SOAPHTTPClient.class);
	private final CloseableHttpClient httpClient;

	@Autowired
	protected Environment env;

	public SOAPHTTPClient() {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(200);
		cm.setMaxTotal(200);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}

	public String execute(String request, String endpointUrl, String soapAction, Boolean isSoap12, String encodedAuth)
			throws UnsupportedEncodingException {
		
		String soapResponseString = null;
		CloseableHttpResponse response = null;
		HttpRequestBase httpRequest = new HttpPost(endpointUrl);
		
		
		try {
				StringEntity input = new StringEntity(request, "UTF-8");
				if(!isSoap12){
					httpRequest.addHeader("SOAPAction", soapAction);
					input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "text/xml;charset=UTF-8"));
				}  else {
					input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, 
							"application/soap+xml;charset=UTF-8;action=\""+soapAction+"\""));
				}
				if(encodedAuth!=null && !encodedAuth.equals("")){
					httpRequest.addHeader("Authorization", "Basic " + encodedAuth);
				}
				input.setContentEncoding("UTF-8");
				((HttpPost) httpRequest).setEntity(input);
				
			response = (CloseableHttpResponse) executeHttpRequest(httpRequest, "POST");

			try {
				soapResponseString = EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
				logger.error("error while parsing SOAP response: " + soapResponseString, e);
			}

		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error("Failed to close HTTP response after invoking: " + endpointUrl, e);
			}

			if (httpRequest != null) {
				httpRequest.releaseConnection();
			} else {
				logger.debug("Could not release connection.");
			}
		}

		return soapResponseString;
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