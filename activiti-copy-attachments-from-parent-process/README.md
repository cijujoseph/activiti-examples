## Demo showing how to pass attachments uploaded in a parent process to a child process and display it on a child process task form
Originally answered at https://community.alfresco.com/thread/224838-pass-attachments-to-sub-process-call-activity

Please note that this is an Alfresco Enterprise feature and require access to the EE repo which is configured in pom.xml.

### How to run the example
1. mvn clean package will generate two artifacts. activiti-copy-attachments-from-parent-process-1.0.0-SNAPSHOT-app.zip and activiti-copy-attachments-from-parent-process-1.0.0-SNAPSHOT-app.jar
2. Import the zip file and publish the App.
3. Place the jar in webapp lib folder.
4. Run the parent process and check the child task form to view the attachment uploaded in parent start form
