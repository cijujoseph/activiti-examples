#### Alfresco Process Service (APS) integration with Amazon SQS. 

This is a very simple extension project/jar demonstrating the integration of [Alfresco Process Services](https://www.alfresco.com/platform/process-services-bpm) with [Amazon Simple Queue Service (SQS)](https://aws.amazon.com/sqs) using [Spring Cloud](http://projects.spring.io/spring-cloud/)

This project is also explained in this [community blog](https://community.alfresco.com/community/bpm/blog/2017/05/23/integrating-alfresco-process-services-with-amazon-sqs-and-apache-activemq)

## Prerequisites
1. This example is built and tested against Alfresco Process Service Version 1.6.1

## Configuration Steps
1. Login to [AWS Console](console.aws.amazon.com/console/home) and create the following queues in SQS
	1. create queue with name "aps-inbound". I will be using this queue to start a process instance by publishing a message to this queue.
	1. create a second queue with name "aps-outbound". I will be using this queue to publish messages from a process instance into SQS.

2. Create a file named "aws-credentials.properties" with the following entries and make it available in the classpath. This file is read by the configuration class AWSSQSConfiguration.java to establish a connection from APS to SQS.
	```
	aws.accessKey=<your aws access key>
	aws.secretKey=<your aws secret key>
	aws.regionName=<aws region eg:us-east-1>
	```
3. Deploy the "aps-aws-sqs-extension-1.0.0-SNAPSHOT.jar" available in this project to activiti-app/WEB-INF/lib
4. Import the "AWS SQS App.zip" in this project into your instance and publish the app.
5. Start a process instance by sending a message to the queue "aps-inbound". Once the message is published, login to APS as "admin@app.activiti.com" (default user) and you will see that a process instance has been started upon a message publish. The process instance is started by the "ListenToSQS.java" component in this project which is basically a SQS listener in APS. In the demo app, the "PublishToSQS.java" class used to send a message to SQS from the process instance is exposed as a BPMN stencil(re-usable custom component) component. I have built the demo process in such a way that, if you use the string "Reply Back" as your input message, the process instance will send a message back to the SQS queue "aps-outbound" immediately. This demonstrates the outbound integration from APS to SQS.

