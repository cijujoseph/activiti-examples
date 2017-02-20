package com.client.bpm.common.bean;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
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

@Component("twilioSMSSend")
public class TwilioSMSSend implements JavaDelegate{

	protected static final Logger logger = LoggerFactory.getLogger(TwilioSMSSend.class);
	private final CloseableHttpClient httpClient;

	@Autowired
	private Environment env;

	public TwilioSMSSend() {

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(200);
		cm.setMaxTotal(200);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}	
	
	public void execute(DelegateExecution execution) throws Exception {
		String jsonString = null;
		String twillioAccountSID = env.getProperty("twillio.account.sid", "XXXX");
		String twillioAuthToken = env.getProperty("twillio.auth.token", "YYYY");
		String authString = twillioAccountSID+ ":"+twillioAuthToken;
		String authorizationString = "Basic "+new String((new Base64()).encode(authString.getBytes()));
		String restEndpointURI = env.getProperty("twillio.sms.url", "https://api.twilio.com/2010-04-01/Accounts/XXXX/Messages.json");
		CloseableHttpResponse response = null;
		HttpRequestBase httpRequest = new HttpPost(restEndpointURI);
		try {
			
			URIBuilder builder = new URIBuilder(restEndpointURI);
			builder.addParameter("To", env.getProperty("twillio.sms.to", "TO_NUM"));
			String twilioNumber = env.getProperty("twillio.number", "FROM_NUM");
			builder.addParameter("From", twilioNumber);
			builder.addParameter("Body","Your car is ready to be picked up. Thank you for servicing with Alfresco Services! If you have a moment, please call "+ twilioNumber +" to answer a short survey.");
			
			URI url = new URI(builder.toString());
			
			UrlEncodedFormEntity entity;
			List<NameValuePair> formparams = URLEncodedUtils.parse(url,"UTF-8");
			entity = new UrlEncodedFormEntity (formparams);
				
			entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
			httpRequest.addHeader("Authorization", authorizationString);
			entity.setContentEncoding("UTF-8");
			((HttpPost) httpRequest).setEntity(entity);
			
			Long httpStartTime = new Date().getTime();
			response = (CloseableHttpResponse) executeHttpRequest(httpRequest);
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
				logger.error("Failed to close HTTP response after invoking: POST " + " "+ restEndpointURI, e);
			}

			if (httpRequest != null) {
				httpRequest.releaseConnection();
			} else {
				logger.debug("Could not release connection.");
			}
		}
		
	}
	
	protected HttpResponse executeHttpRequest(HttpRequestBase httpRequest) {

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpRequest);
		} catch (IOException e) {
			throw new ActivitiException("error while executing http request: POST " + " "+ httpRequest.getURI(), e);
		}

		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new ActivitiException("error while executing http request: POST  " + " "+ httpRequest.getURI()
					+ " with status code: " + response.getStatusLine().getStatusCode());
		}
		return response;
	}

}
