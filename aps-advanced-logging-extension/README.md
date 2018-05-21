* Import the app MDCTest.zip into APS and publish the app.
* Deploy aps-advanced-logging-extension-1.0-SNAPSHOT.jar to APS classpath
* Update the log4j config which you can find in activiti-app/WEB-INF/classes directory with the following two configurations
	* Set the org.springframework.web.filter.CommonsRequestLoggingFilter logging leve to DEBUG if you like to log all the API requests. For more details on this implementation please refer http://www.baeldung.com/spring-http-logging
	* Check if the logging pattern includes %X, if yes you are good to log the logged in user in all the log entries using the MDC logic present in this project. For more details refer http://www.baeldung.com/mdc-in-log4j-2-logback.
* Start the process and check the logs. You can use the following curl command to start a process. As you can see, the logs now include "user=Ciju Administrator" in all the log entries that happened in the same thread! The last line doesn't have that because if you look at the process that you imported in the first step, it is a log entry from an "async" service task where there is no user session involved. 

`curl http://localhost:8080/activiti-app/api/enterprise/process-instances -X POST -H "Content-Type: application/json" -u admin@app.activiti.com:admin --data '{"processDefinitionKey":"MDCTest","name":"MDCTest - Process"}'`

> 09:48:18 [http-nio-8080-exec-48] DEBUG org.springframework.web.filter.CommonsRequestLoggingFilter **user=Ciju Administrator** - Before request [uri=/activiti-app/api/enterprise/process-instances;client=0:0:0:0:0:0:0:1;user=admin@app.activiti.com;headers={host=[localhost:8080], authorization=[Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTphZG1pbg==], user-agent=[curl/7.54.0], accept=[*/*], content-length=[61], Content-Type=[application/json;charset=UTF-8]}]

> 09:48:18 [http-nio-8080-exec-48] INFO  org.activiti.engine.impl.bpmn.deployer.BpmnDeployer **user=Ciju Administrator** - Processing resource mdctest.bpmn

> 09:48:19 [http-nio-8080-exec-48] INFO  org.activiti.engine.impl.bpmn.deployer.BpmnDeployer **user=Ciju Administrator** - Processing resource mdctest.MDCTest.png

> 09:48:19 [http-nio-8080-exec-48] INFO  com.activiti.extension.bean.TestMDCJavaDelegate **user=Ciju Administrator** - logging to check MDC info in the logged line

> 09:48:19 [http-nio-8080-exec-48] DEBUG org.springframework.web.filter.CommonsRequestLoggingFilter **user=Ciju Administrator** - After request [uri=/activiti-app/api/enterprise/process-instances;client=0:0:0:0:0:0:0:1;user=admin@app.activiti.com;headers={host=[localhost:8080], authorization=[Basic YWRtaW5AYXBwLmFjdGl2aXRpLmNvbTphZG1pbg==], user-agent=[curl/7.54.0], accept=[*/*], content-length=[61], Content-Type=[application/json;charset=UTF-8]};payload={"processDefinitionKey":"MDCTest","name":"MDCTest - Process"}]

> 09:48:19 [pool-20-thread-1] INFO  com.activiti.extension.bean.TestMDCJavaDelegate  - logging to check MDC info in the logged line


****
####Please note that this project is only an example. You will need to review and adjust the project to meet your logging needs. Also, make sure you are not logging any sensitive information into the logs!