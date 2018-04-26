### A custom REST endpoint that injects adhoc subprocess into an existing parallel multi-instance call activity loop.

1. Deploy aps-adhoc-subprocess-injection-into-multi-instance-call-activity.jar to the APS classpath
2. Import and publish aps-adhoc-subprocess-injection-into-multi-instance-call-activity.zip to APS
3. Start the parent process and it will spawn two subprocess as configured (the subprocess process definition key will be "mypd")
4. Now, if you  want to inject a third subprocess with key "my-new-pd" into this existing multi-instance loop invoke the following REST API which is part of this extension library!

	`curl http://localhost:8080/activiti-app/api/enterprise/dynamicsubprocess -X POST -H "Content-Type: application/json" -u admin@app.activiti.com:admin --data '{"processInstanceId": "107740", "activityId": "call-activity-1","processDefinitionKey": "my-new-pd"}'`

	OR, if you know the parent execution id of the call activity step, do the following!

	`curl http://localhost:8080/activiti-app/api/enterprise/dynamicsubprocess -X POST -H "Content-Type: application/json" -u admin@app.activiti.com:admin --data '{"executionId": "100045","processDefinitionKey": "my-new-pd"}'`

