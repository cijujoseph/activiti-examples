#### Alfresco Process Service (APS) integration with Apache ActiveMQ (this can be extended to any JMS providers). 

This is a very simple extension project/jar demonstrating the integration of [Alfresco Process Services](https://www.alfresco.com/platform/process-services-bpm) with [Apache ActiveMQ](http://activemq.apache.org/)

## Prerequisites
1. This example is built and tested against Alfresco Process Service Version 1.6.1 and ActiveMQ 5.14.5
2. Apache ActiveMQ must be installed and started. The extension project is built using the default credentials & ports which Active MQ use which are port 61616 and admin/admin respectively. Since I haven't externalized these properties you will need to recompile this project if you have a different connection setting.

## Configuration Steps

1. Deploy all the jar files available in this project to activiti-app/WEB-INF/lib
4. Import the "ActiveMQ App.zip" in this project into your instance and publish the app.
5. Start a process instance by sending a message to the queue "aps-inbound". Once the message is published, login to APS as "admin@app.activiti.com" (default user) and you will see that a process instance has been started upon a message publish. The process instance is started by the "ActiveMQListener.java" component in this project which is basically a JMS listener in APS. In the demo app, the "ActiveMQSender.java" class used to send a message to ActiveMQ from the process instance is exposed as a BPMN stencil(re-usable custom component) component. I have built the demo process in such a way that, if you use the string "Reply Back" as your input message, the process instance will send a message back to the JMS queue "aps-outbound" immediately. This demonstrates the outbound integration from APS to ActiveMQ.

