## Quick and dirty instructions, to be updated in detail...

### Steps to deploy and run using a gmail account

* Create a Gmail account (recommend creating an account for testing this instead of using your personal account)
* Allow less secure apps by going to https://myaccount.google.com/lesssecureapps
* Open email-listener.properties file available in the root directory and update the username/pw properties with your account details.
* Place the updated file (email-listener.properties) in webapps/activiti-app/WEB-INF/classes
* Import the process app archive (Email Receiver.zip) via App Designer -> Apps -> Import App
* Deploy the app by clicking on the "Publish" button.
* Copy all the 4 jar files available in the root directory to webapps/activiti-app/WEB-INF/lib
* Restart Alfresco Process Services (if not configured to start automatically on classpath updates).
* Send an email along with few attachments to your gmail account and verify that a process is created in APS corresponding to the email and the email is marked as read in your gmail account!


