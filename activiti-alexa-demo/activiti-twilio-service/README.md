### A micro service that can handle Twilio incoming calls and talk to the APIs of Activiti, Decooda etc. For more details on TwiML and the associated Java APIs, please refer to the Java quick start available at https://www.twilio.com/docs/quickstart/java/twiml

## Usage of this webapp in the demo

1. Modify the properties in twilio-app.properties (properties explaied below) which is kept in src/main/resources.
2. mvn clean package
3. Place the generated activiti-twilio-service.war in tomcat/webapps/
4. Make sure that the webapp has started correctly by invoking the first API (http://localhost:8080/activiti-twilio-service/hello) which Twilio invokes upon receiving a call!
5. Configure the above URL by following instructions at https://support.twilio.com/hc/en-us/articles/223136207-Getting-started-with-your-new-Twilio-phone-number. Please note that the URL should be a publicly(internet) accessible url and NOT localhost when configuring in Twilio! 


## Configuration Details Explained
```
twilio.callercheck.enabled - Value can be true or false. If set to true, caller will get a not recognized message if the number is not configured in the property file!
twilio.caller.id.n - configure the name and number delimited by a semicolon. Replace "n" with a number. eg: twilio.caller.id.1=Ciju J;+1XXX

decooda.enabled - value can be true or false. This is the cognitive analytics platform which I use to analyse the recorded voice of a customer. Contact https://decooda.com/contact-us/ if you want to enable this in your process. Otherwise set it to false which will mock the decooda response
deccoda.api.url - decooda API
decooda.model - decooda model number to use to analyse the customer response.

activiti.api.base.url - Alfresco Activiti base url http://\<user\>:\<pw\>@\<host\>:\<port\>/activiti-app/api/
```
