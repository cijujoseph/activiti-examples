## A demonstration of the DMN usage (central configuration) for dynamic user assignment.


### Steps to run this example

*   Login to Alfresco Process Services and create a user named -> devuser@example.com
*	Import and publish the app named "DMN Deployment.zip" which will deploy the rules
*	Import and publish the app named "UserLookupExample.zip" which will demonstrate the use of rules for user assignment. If you look at the assignment expression on the User Task in the  process "UserLookupExample", you will see the use of expression to resolve the assignee at runtime
*	To test the rule, run the process "UserLookupExample" by entering "process-1" as ProcessName and "prod" as Environment in the start form. Based on these values, the rules will be executed and the task assignee will be set to admin@app.ativiti.com
*	If you run this process with any other data task will go to devuser@example.com


