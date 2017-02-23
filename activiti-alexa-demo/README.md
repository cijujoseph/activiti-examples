##Work in progress component - NOT YET WORKING E2E!

#### The project contains all the components of my blog post - Voice Enabled Business Process using Alfresco Activiti and AWS Alexa

### Prerequisites to run this demo end-2-end

* Alfresco Activiti Enterprise (Version 1.5 and above) - If you don't have it already, you can download a 30 day trial from https://www.alfresco.com/products/business-process-management/alfresco-activiti
* Alfresco One - I am using Alfresco One to store the contents associated with the process. Please check https://www.alfresco.com/products/enterprise-content-management/one for more details. If you don't want to use Alfresco One, you can modify the process model and switch over to GoogleDrive/Box which is also supported out of the box in Alfresco Activiti. You could even remove the content publish step from the process model if you don't want to see that part in action!
* An Amazon Echo Device - If you don't have one already, you may need to buy one. The economical option is to buy an Echo Dot!
* An Amazon Account which you would use to configure your Echo Device, create Alexa Skill Set (https://developer.amazon.com/alexa-skills-kit
), host the Alexa Service on AWS Lambda (https://aws.amazon.com/lambda/) etc
* A Twilio Account (https://www.twilio.com/) for voice and messaging capabilities.

## Configuration Steps

### Activiti Setup and Process Deployment
1. Setup Alfresco Activiti if you don't have one already. Instructions & help available at http://docs.alfresco.com/activiti/docs/, https://community.alfresco.com/community/bpm 
2. Configure Activiti to work with an extenal content repository - https://docs.alfresco.com/activiti/docs/admin-guide/1.5.0/#_integration_with_external_systems
3. Modify and copy the client-bpm.properties from demo-resources folder to into activiti-app/WEB-INF/classes
4. Copy the activiti-process-components-1.0.0-SNAPSHOT.jar from demo-resources folder to activiti-app/WEB-INF/lib.
3. Configure a custom email template by following instruction in custom-email-template.txt in demo-resources folder of this project
4. Import the Service Booking.zip app available in the demo-resources folder of this project into Activiti.
5. Modify the process to fix the content publish step as required.
6. Publish/Deploy the App

### Twilio Setup Configuration
1. Signup for an account https://www.twilio.com/try-twilio.
2. Obtain a free twilio number
3. Follow the README available in activiti-twilio-service folder of this project.

### Alexa Configuration
1. Follow the README available in ctiviti-alexa-service folder of this project.
2. Your Echo device and Alexa skills must be configured using the same account.

#### Important Notes:
* Activiti and Twilio webapps must be accessible from the public (for Twilio to call our twilio api, alexa to talk to activiti etc). I normally use ngrok (https://ngrok.com/) to make these apps available on internet. The following two commands will do the job for you! Another option is to do everything on an EC2 instance. 
```
npm install -g ngrok
ngrok http 8080
```
* Activiti must have an email server configured via activiti-app.properties

