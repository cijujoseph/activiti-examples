package com.activiti.extension.conf;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/*
 * Reading the AWS credentials from properties file. For production implementations please refer 
 * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html for AWS best practices
 */

@Configuration
@PropertySource("classpath:aws-credentials.properties")
public class AWSConfiguration {

	@Value("${aws.accessKey}")
	private String accessKey;

	@Value("${aws.secretKey}")
	private String secretKey;

	@Value("${aws.regionName}")
	private String regionName;

	@Bean
	public DynamoDB dynamoDB() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonDynamoDB amazonDynamoDBClient = AmazonDynamoDBClientBuilder.standard()
							.withCredentials(new AWSStaticCredentialsProvider(credentials))
							.withRegion(Regions.fromName(regionName))
							.build();
		return new DynamoDB(amazonDynamoDBClient);
	}

}
