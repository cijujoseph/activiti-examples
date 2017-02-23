

I will not go into the basics of alexa skills & lambda use in this readme. https://github.com/alexa/skill-sample-nodejs-howto is a great resource that can help you build and configure Alexa Skills.

## Demo specific configuration - Alexa Skills

* Intent Schema Configuration - Use the following intent schema. In the below intent, you could also use AMAZON.Person instead of CUSTOM_NAME_LIST
```
{
  "intents": [
    {
      "intent": "Available",
      "slots": [
        {
          "name": "status",
          "type": "LIST_OF_STATUS"
        }
      ]
    },
    {
      "intent": "Goodbye"
    },
    {
      "intent": "Hello",
      "slots": [
        {
          "name": "name",
          "type": "CUSTOM_NAME_LIST"
        }
      ]
    },
    {
      "intent": "NeedHelp"
    },
    {
      "intent": "ScheduleService"
    },
    {
      "intent": "CheckApmntDate"
    },
    {
      "intent": "RescheduleApmt",
      "slots": [
        {
          "name": "days",
          "type": "AMAZON.NUMBER"
        }
      ]
    },
    {
      "intent": "CancelApmt"
    }
  ]
}
```
* Custom Slot Types
```
#Type
LIST_OF_STATUS	
#Values 
up
down

#Type - required only if you using this slot type
CUSTOM_NAME_LIST
#Values - ENter all the possible name values you want to enter here
```

* Sample Utterances
```
Available is alfresco {status}
Goodbye goodbye
Hello My name is {name}
NeedHelp I need help
ScheduleService Schedule a service
CheckApmntDate Checking my appointment date
RescheduleApmt Change my appointment by {days} days
RescheduleApmt Change my appointment date
CancelApmt Cancel my appointment
```
* Configuration
Service Endpoint Type - Select AWS Lambda ARN (Amazon Resource Name). Now copy the ARN of your Lambda function which you might have created already. Otherwise go ahead and create one by following the instructions below.

## Create AWS Lambda Function

1. This project is a maven based project, build using the command "mvn assembly:single" which will generate a zip file which you can use in the next step. Thanks to Greg's code which I used as a reference - https://github.com/melahn/alexa-alfresco
2. Go to AWS Management Console -> "Lambda" -> Create a Lambda Function -> Skip Select a Blueprint -> Go to next "Configure triggers" tab -> Select "Alexa Skills Kit" from the list (not all reagios provide this option. I had to use N. Virginia) -> Click Next->Select "Upload a .ZIP file" as Code entry type" -> Give a Name to your function -> Upload the zip generated in the previous step -> Enter "activiti-alexa-service.handler" as Handler -> Choose a role (check the howto guide for more on roles)->Click Next-> Click Create Function-> Notedown the ARN that appears on the top
If function creation was successful, you would be able to see the activiti-alexa-service.js code in the project in the Lambda console. Now go ahead and edit the variables declared at the top to match your environment. Please note the following two:
	* if "alfrescoActivitiVersion" is less than 1.5.3.2 you may need to update the "serviceBookingProcessDefinitionId" everytime you modify the business process in Activiti.
	* appId must be correct set to match the Alexa Skill ID. 
3. Once you have the created the Lambda function you may want to go back to your Skills and update the ARN in your Alexa configuration

Good to go!



