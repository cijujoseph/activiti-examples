
## A custom extension which allows users in a group to access all tasks in the system (tested and verified in Alfresco Process Services 1.7)

### Configuration 

* To configure this extension it is easy, all you have to do is place the aps-task-watchlist-extension-1.0.0-SNAPSHOT.jar file in webapps/activiti-app/WEB-INF/lib directory. Obviously if you are modifying it, you will need to repackage the jar!
* Create a group named "watchlist-group" and add users to the group. Users in this group will have access to all the tasks in the system.

If you know the task id/process instance id, it is also possible to see the task/process details using the following urls in Alfresco Process Services UI without browsing through the task/process list. This will be useful if you want to generate emails with urls in the email body which will help users to go straight to the task/process details.

1. http://\<host\>:\<port\>/activiti-app/workflow/#/process/\<processInstanceId\>
2. http://\<host\>:\<port\>/activiti-app/workflow/#/task/\<taskId\>



