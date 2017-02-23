
### An extension project which does the email and text notification in the demo process.

The project contains two Java Delegates and some helper classes. EmailAndTextNotification.java is the delegate that does both text and email notification based on properties set in a property file called "client-bpm.properties". Check the demo-resources folder of this project for a sample property file. You would need access to Alfresco Enterprise repository to build this project from scratch. Hence I tried to externalise all the possible configurations and built a jar (available in demo-resources folder) which can be used to run the demo by yourself. All you have to do is place the jar file in webapps/activiti-app/WEB-INF/lib and place the property file in the webapps/activiti-app/WEB-INF/classes folder!

#### Note:
1. Must have the email server configured via activiti-app.properties
2. A valid twilio account is required to send SMS
