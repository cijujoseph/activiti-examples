
## A custom extension which allows users in a group to access all tasks in the system (tested and verified in Alfresco Process Services 1.7+)

### Configuration 

* To configure this extension it is easy, all you have to do is place the aps-task-watchlist-extension-1.0.0-SNAPSHOT.jar file in webapps/activiti-app/WEB-INF/lib directory. Obviously if you are modifying it, you will need to repackage the jar!

## Option 1: Add users/groups as PARTICIPANTS to processes/tasks using event listeners:

### Watch all tasks in the system
* Create a group named "watchlist-group" and add users to the group. Users in this group will have access to all the tasks in the system. This is implemented in TaskWatchlistListener.java which is a TASK_CREATED event listener. This class will listen to all new task creations in the system and adds this group as a participant to the newly created task.


### Watch all processes in the system (Works only from APS 1.8+)

* Create a group named "process-watchlist-group" and add users to the group. Users in this group will have access to all the processes in the system. This is implemented in ProcessWatchlistListener.java which is a PROCESS_STARTED event listener. This class will listen to all new process instance creations in the system and adds this group as a participant to the newly created process instance.

## Option 2: Override the PermissionService with custom rules:
CustomPermissionService.java is an example showing how to override the default permission service of APS. In this example, I'm overriding the service to let users in watchlist-group & process-watchlist-group to access all processes/tasks in the system. Using this approach one doesn't have to add those groups as "Participant" to all processes/tasks using event listeners. This is useful in scenarios where you know the task id/process instance id, and wants to see the details of the process/task without browsing through the task/process list. Eg: Generate emails with urls in the email body which will help admin users to go straight to the task/process details.

1. http://\<host\>:\<port\>/activiti-app/workflow/#/process/\<processInstanceId\>
2. http://\<host\>:\<port\>/activiti-app/workflow/#/task/\<taskId\>





