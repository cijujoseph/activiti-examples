#### Alfresco Process Service integration with Amazon DynamoDB using Data Model. 

This project is a replica of [activiti-custom-data-model-sample](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-custom-data-model-sample) and I replaced Elasticsearch with Amazon DynamoDB data source. To get an overview of the parent project, please refer [Business Data Integration Made Easy With Data Models](https://community.alfresco.com/community/bpm/blog/2017/04/12/business-data-integration-made-easy-with-data-models)

This project and its setup is also explained at - <placeholder for a blog post!>

##Prerequisites
1. This example is built and tested against Alfresco Process Service Version 1.6.1

## Configuration Steps
1. Create the following two tables in Amazon DynamoDB.
	1. TableName: Policy, PrimaryKey: policyId
	2. TableName: Claim, PrimaryKey: claimId
2. Create a file named "aws-credentials.properties" with the following entries and make it available in the classpath
	```
	aws.accessKey=<your aws access key>
	aws.secretKey=<your aws secret key>
	aws.regionName=<aws region eg:us-east-1>
	```
3. Deploy the "aps-dynamodb-data-model-1.0.0-SNAPSHOT.jar" available in this project to activiti-app/WEB-INF/lib
4. Import the "InsuranceDemoApp.zip" in this project into your instance and publish the app.
5. Run and observe the process integration with DynamoDB. Once you run a process end-end, login to AWS console, you will find that the data is created in DynamoDB

