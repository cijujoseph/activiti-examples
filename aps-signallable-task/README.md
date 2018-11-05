
### An example showing how to implement an async req/reply pattern in Activiti/APS. 

An example use case is a JMS Request/Reply. Sending a JMS message from a Service Task and waiting for the reply before proceeding to the next step in the business process. 

In this bpmn example the service task is implemented using the  org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior interface instead of standard org.activiti.engine.delegate.JavaDelegate.

SignallableActivityBehavior contains the following two methods:

1. execute(ActivityExecution execution) - where the actual JMS Send logic can be written.
2. signal(ActivityExecution execution, String signalEvent, Object signalData) which the JMS listener can call to trigger the process continuation. The signal can be triggered using the standard signal API (JAVA/REST) in APS.


### How to run this example:
1. Deploy the jar to place the aps-signallable-task-1.0-SNAPSHOT.jar file in webapps/activiti-app/WEB-INF/lib directory.
2. Import the process app signallable-task-example.zip and start a process.
3. Go to the log file and on successful start of a process instance, the ExecutionID should have been written to the log file.
4. Use the execution ID to trigger the following API which will complete the task and help move the process to the next step.

```
PUT http://localhost:8080/activiti-app/api/runtime/executions/${executionId}?tenantId=tenant_1
{
  "action":"signal",
  "variables" : []
}```
