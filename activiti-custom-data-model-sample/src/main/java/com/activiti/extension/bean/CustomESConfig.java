package com.activiti.extension.bean;

import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.springframework.stereotype.Component;

import com.activiti.api.elasticsearch.ElasticSearchNodeSettingsConfigurer;

//This component is to enable the http.compression property of the embedded 
//Elasticsearch so that the APIs can be invoked via our datamodel

@Component
public class CustomESConfig implements ElasticSearchNodeSettingsConfigurer {

	@Override
	public void elasticSearchSettingsInitialized(Builder settingsBuilder) {
		settingsBuilder.put("http.compression", true);

	}

}