package com.activiti.extension.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.activiti.domain.idm.EndpointBasicAuth;
import com.activiti.domain.idm.EndpointConfiguration;
import com.activiti.service.api.EndpointService;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

public class BpmRestProxyResourceTest {
	
	@InjectMocks
	private BpmRestProxyResource bpmRestProxyResource;
	
	@Mock
	EndpointService endpointService;
	
    @Autowired
    private Environment env;
	
	@ClassRule
	public static WireMockClassRule wireMockRule = new WireMockClassRule(18089);

	@Rule
	public WireMockClassRule instanceRule = wireMockRule;
	
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		List<EndpointConfiguration> endpointConfigurations = new ArrayList<EndpointConfiguration>();
		EndpointBasicAuth auth = new EndpointBasicAuth();
		auth.setId(1L);
		auth.setName("TEST");
		auth.setUsername("TEST");
		auth.setPassword("TEST");
		EndpointConfiguration endpoint = new EndpointConfiguration();
		endpoint.setId(1L);
		endpoint.setHost("localhost");
		endpoint.setPort("18089");
		endpoint.setBasicAuth(auth);
		endpointConfigurations.add(endpoint);
		when(endpointService.getConfigurationsForTenant(anyLong())).thenReturn(endpointConfigurations);
		
		
		String ITSUsers = IOUtils.toString(
				BpmRestProxyResourceTest.class.getResourceAsStream("/json/users.json"));
		String BPMUserManagersLK = IOUtils.toString(
				BpmRestProxyResourceTest.class.getResourceAsStream("/json/users.json"));
		
		// Users
		stubFor(get(urlEqualTo("/rest/bpm/esb/user?queryparam1=XYZ"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(ITSUsers)));
		// BPM Managers
		stubFor(get(urlEqualTo("/rest/bpm/esb/user/paul/manager"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(BPMUserManagersLK)));
	}
	
	@Test
	public void testUsers() throws IOException, RuntimeException {
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		String result = IOUtils.toString(httpClient.execute(
						new HttpGet("http://localhost:18089/rest/bpm/esb/user?queryparam1=XYZ")
				).getEntity().getContent());

		System.out.println(result);
		assertNotNull(result);
		assertThat(result, containsString("CIJU"));
	}

	@Test
	public void testBPMUserManagers() throws IOException, RuntimeException {
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		String result = IOUtils.toString(httpClient.execute(
						new HttpGet("http://localhost:18089/rest/bpm/esb/user/paul/manager")
				).getEntity().getContent());

		System.out.println(result);
		assertNotNull(result);
		assertThat(result, containsString("CIJU"));
	}
}