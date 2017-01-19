
### An event listener based custom email generation project for Activiti EE 

## How to run this example

1. Place the three freemarker templates in webapps/activiti-app/WEB-INF/classes/email-templates.
2. Place the activiti-custom-email-extension-1.0.0-SNAPSHOT.jar file in webapps/activiti-app/WEB-INF/lib
3. Import activiti-custom-email-extension-1.0.0-SNAPSHOT-app.zip project via ActivitiUI->Kickstart App->Apps->Import App
4. Publish the App and Run. Check the email in your inbox!

#### Note:
1. Must have the email server configured via activiti-app.properties
2. The task assignee must have a valid email id.


## Configuration Details

activiti-custom-email-extension.zip - is the source of activiti-custom-email-extension-1.0.0-SNAPSHOT.jar. It contains a class called "CustomTaskAssignmentListener" which is a custom event listener which listens for "Task Assigned" events. When a task assigned event is received, it looks up the task-definition id and check if it needs to use a particular template for sending email. Depending on the configuration, it selects the appropriate template and sends and email.
The other classes in the project are few helper classes to help with the email processing and variable creation!

Activiti version 1.5 onwards, you can define email templates in Activiti via the Identity Management Module. If you are using a version greater than 1.5, highly recommend to utilize this feature and recompile the EmailUtils.java class by modifying the evaluateTemplate() method. 1.5 compatible code snippet is already kept commented in the source.

