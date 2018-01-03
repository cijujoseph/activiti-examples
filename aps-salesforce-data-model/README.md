
# Alfresco Process Service integration with Salesforce using Data Model. 

### Salesforce Account Setup - Prerequisite
* If you don't already have an account, create a developer account in Salesforce (SFDC). Sign-up by clicking [here](https://developer.salesforce.com/signup)

>Tip: if you are using a gmail account for signing up and would like to re-use the same email id for multiple SFDC accounts, append a plus ("+") sign and any combination of words or numbers after your email id.
eg: youremail+demo1@gmail.com, youremail+demo3@gmail.com, youremail+demo2@gmail.com etc

* [Get a security token](https://help.salesforce.com/articleView?id=user_security_token.htm)
* Create a new Connected App and obtain a consumer key and consumer secret which can be used as the clientId and clientSecret in API calls. 

>Tip: 
>If using Salesforce Classic view - Setup -> Create -> Apps -> Connected Apps -> New 

>If using Lightning Experience view - Setup -> Apps -> App Manager -> New Connected App

>For more details [click here](https://help.salesforce.com/articleView?id=000205876&type=1)

### Configure & Deploy the property file 

* Once the above steps are completed, open extension-bpm.properties file available in the root directory and update the following 4 properties with your account details.

```
sfdc.username=use your login email

sfdc.password=concatenate password and security token

sfdc.clientId=consumer key

sfdc.clientSecret=consumer secret
```

* Place the updated file (extension-bpm.properties) in webapps/activiti-app/WEB-INF/classes

### Deploy the extension jar file

* Place the jar file (aps-salesforce-data-model-1.0-SNAPSHOT.jar) in webapps/activiti-app/WEB-INF/lib
* Restart Alfresco Process Services (if not configured to start automatically on classpath updates).


### Deploy the process app.
* Import the process app archive (SFDC Data Model App.zip) via App Designer -> Apps -> Import App
* Deploy the app by clicking on the "Publish" button.

### Running the demo

Start the process using a valid Account Number from your Salesforce account. Account details will be fetched from Salesforce via Data Model and shown on the User Task in the process. Upon completion of the User Task, a new Case will be created in Salesforce using Data Model.

For more details watch below video where I captured the development and a demo run of the attached example process app (SFDC Data Model App.zip).

<a href="http://www.youtube.com/watch?feature=player_embedded&v=WbWAZK_5mxU" target="_blank"><img src="https://img.youtube.com/vi/WbWAZK_5mxU/0.jpg" alt="Click here to watch a demo of this project" width="240" height="180" border="10" /></a>


### Creating/Updating Data Model Entities and Customizing this demo to work with all Salesforce objects
The extension is a generic extension that should work with all Salesforce objects **without you writing additional code**. You should be able to create  Salesforce data model entities in APS (such as Account, Case, Contract, Campaign etc) or add more attributes to existing data model entities by referring this page - https://developer.salesforce.com/docs/atlas.en-us.object_reference.meta/object_reference/sforce_api_objects_list.htm. The data model entity name must match the "Standard Object" name & entity attribute names must match the field names listed in the api reference page. 

**Note :** - Certain fields/attributes may be write protected in Salsforce. In such cases, create/update on those attributes will fail if attempted!



