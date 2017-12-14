## Demo showing how to invoke rules from a task/start form or from an exteranl system

### Solution Approach
1. Define a custom rest endpoint which can run DMN rules in Activiti and respond back with results.
2. Invoke the Custom REST endpoint from UI via Javascript do the data validation on UI end.

### How to run the example
1. mvn clean package will generate two artifacts. activiti-dmn-extension-1.0.0-SNAPSHOT-app.zip and activiti-dmn-extension-1.0.0-SNAPSHOT.jar
2. Import the activiti-dmn-extension-1.0.0-SNAPSHOT-app.zip app and publish the App.
3. Place the activiti-dmn-extension-1.0.0-SNAPSHOT.jar in webapp lib folder.
4. Run the process named My-Process and try submitting the form by filling in the request form. The form submit will not work for age<21 and it is controlled by my DMN Rule.

## Test via API
```
POST http://host:port/activiti-app/api/enterprise/dmn/agerule/evaluate
Body {"personAge":20}
```

### Components in the example:
1. activiti-dmn-extension.zip - Java project containing two REST classes. Providing the following two REST apis to run our DMN Rules. 
    1. http://localhost:8080/activiti-app/app/rest/dmn/{key}/evaluate - API is secured with our activiti-app cookie approach and can be used to do form validation. 
    2. http://localhost:8080/activiti-app/api/enterprise/dmn/{key}/evaluate - is secured using basic auth and can be used if you want to invoke rules from other systems, for example ESB!

2. activiti-dmn-extension-1.0.0-SNAPSHOT.jar - Jar file from the above java project.
3. activiti-dmn-extension-1.0.0-SNAPSHOT-app.zip: contains two processes & one decision table.
	1. Rule-Container Process - Using it to deploy my decision table.
	2. My-Process Process - I am demonstrating the client side rules execution in this process. In the start form of this process, I have embedded some javascript which demonstrates the rules execution via my custom API. Since the Activiti DML endpoints have CSRF security check applied, a CSRF token needs to be supplied to make it work. That is also provided in the javascript sample. In this case validation happens on form-submit. Technically this can be applied on any UI events including form field change. 
	3. age-rule-check Decision Table - Checking if the age provided is less than 21, if yes return underAge.
