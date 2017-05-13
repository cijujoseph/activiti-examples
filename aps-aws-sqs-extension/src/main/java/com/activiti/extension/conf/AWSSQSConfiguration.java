package com.activiti.extension.conf;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:aws-credentials.properties")
public class AWSSQSConfiguration {
	
	@Value("${aws.accessKey}")
	private String accessKey;

	@Value("${aws.secretKey}")
	private String secretKey;

	@Value("${aws.regionName}")
	private String regionName;

	@Bean
	public AmazonSQSAsync amazonSQSClient() {
		
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		
		AmazonSQSAsync amazonSQSClient = AmazonSQSAsyncClientBuilder.standard()
							.withCredentials(new AWSStaticCredentialsProvider(credentials))
							.withRegion(Regions.fromName(regionName))
							.build();
		return amazonSQSClient;
		
	}

	@Bean
	public QueueMessagingTemplate queueMessagingTemplate() {
		return new QueueMessagingTemplate(amazonSQSClient());
	}

	@Bean
	public SimpleMessageListenerContainer simpleMessageListenerContainer() {
		SimpleMessageListenerContainer msgListenerContainer = simpleMessageListenerContainerFactory()
				.createSimpleMessageListenerContainer();
		msgListenerContainer.setMessageHandler(queueMessageHandler());

		return msgListenerContainer;
	}

	@Bean
	public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory() {
		SimpleMessageListenerContainerFactory msgListenerContainerFactory = new SimpleMessageListenerContainerFactory();
		msgListenerContainerFactory.setAmazonSqs(amazonSQSClient());
		msgListenerContainerFactory.setMaxNumberOfMessages(1);

		return msgListenerContainerFactory;
	}

	@Bean
	public QueueMessageHandler queueMessageHandler() {
		QueueMessageHandlerFactory queueMsgHandlerFactory = new QueueMessageHandlerFactory();
		queueMsgHandlerFactory.setAmazonSqs(amazonSQSClient());

		QueueMessageHandler queueMessageHandler = queueMsgHandlerFactory.createQueueMessageHandler();

		return queueMessageHandler;
	}
}
