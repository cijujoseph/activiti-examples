#### This is a very simple client side ADF App (end user facing) built on top of a couple of business processes in Alfresco Process Services (APS) and contents in Alfresco Content Services (ACS). 

## Prerequisites
1. Alfresco Process Services powered by Activiti and optionally Alfresco Content Services. This example is tested against APS 1.6.1 and ACS 5.2.
2. Basic understanding of [Angular2+](https://angular.io/), [Alfresco ADF](https://community.alfresco.com/community/application-development-framework/pages/get-started), NodeJS etc

## Solution Architecture
This ADF application is built as a component ("ADF Application") in the below solution diagram. This is a typical solution architecture that is found in a lot of enterprises.
![alt tag]( https://github.com/cijujoseph/activiti-examples/blob/master/adf-samples/insurance-policy-and-claim-center-app/demo-resources/Architecture.png )
Components in the architecture are:
* ADF Application: End user facing application that can be built very quickly using the Alfresco Application Development Framework on top of all the following components in an enterprise.
* Alfresco Process Services: Platform to model and run the business processes.
* Alfresco Content Services: Platform to manage enterprise contents/documents.
* System Of Record: Various business data stores in an enterprise. eg: Salesforce, SAP, Databases etc 
* Business Intelligence Warehouse: Enterprise wide BI platform. Not included in this demo app setup, however possible to utilize them in the ADF application.

## Configuration Steps
1. Import one of the following two process apps into your APS instance (APS UI->App Designer->Apps->Import App) and publish the app. Once the app is published/deployed, go to the landing(home) page of APS and make sure that the app named "InsuranceProcessSuite" is added to the landing page. 
	```
	> demo-resources/InsuranceProcessSuite_With-ACS-Integration.zip 
							*OR*
	> demo-resources/InsuranceProcessSuite.zip. (The processes in this app are built without ACS integration. Use this if you do not have ACS installed.)
	```
2. Since I'm using a custom data model using the embedded Elasticsearch instance of APS, we need to enable the HTTP endpoint of APS Elasticsearch by modifying the following two properties in the activiti-app.properties file. For more details on this component and configuration, please refer [activiti-custom-data-model-sample](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-custom-data-model-sample)
	```
	elastic-search.enable.http=true
	elastic-search.enable.http.cors=true
	```
3. Deploy the "activiti-custom-data-model-sample-1.0.0-SNAPSHOT.jar" available in this project to activiti-app/WEB-INF/lib. This is the implementation library of the above mentioned custom data model.
4. Import the following forms into your APS instance. These forms are used in the ADF application to display the records from the System of Record (Elasticsearch).
	1. demo-resources/Insurance_Form_ADF.json
	2. demo-resources/Property Claim - ADF.json
5. Update the insurance-demo-adf-app/environments/environment.ts file with your environment details. Please find below the explanation of the properties used in this file
	```
	 > providers -> Use 'BPM' if you donot have ACS installed and configured to work with APS. If both ACS and APS are available, use 'ALL'
  	
	 > ecmUrl -> The base url of ACS. Ignored if providers is set to 'BPM'
    
	 > adminGroupName -> The group name which we will create in APS in step 7 below.
  	
	 > bpmUrl: The base url of APS.
  	
	 > elasticsearchUrl: the base url of Elasticsearch (embedded ES in APS) which is used as the System of Record in the demo app.
  	
	 > insuranceDocumentsRootNodeId: The nodeId of root folder in ACS which is used to publish documents from APS. Ignored if providers is set to 'BPM'
  	
	 > claimDetailsFormId: The ID of the claim form that is imported in step 4 above. APS UI->App Designer->Forms->"Property Claim - ADF"->Check url for the form ID
  	 
	 > policyDetailsFormId: The ID of the policy form that is imported in step 4 above. APS UI->App Designer->Forms->"Insurance_Form_ADF"->Check url for the form ID
  	 
	 > processAppId: The Runtime ID of the imported (step 1) "InsuranceProcessSuite" process application. Check the URL by clicking on "APS Home Page -> "InsuranceProcessSuite"->Check url for the App ID
	```
6. Start the ADF application by executing the following commands.
	```
	cd insurance-demo-adf-app
	npm install
	npm start
	```
7. Create a group called "admin-group" in APS->Identity Management->Organization and add a user to this group.
8. Access the ADF app by going to http://localhost:3000.

## Demo App Highlights
1. Mobile friendly app!
2. Control access permissions centrally via groups defined in APS/LDAP
	Eg: admin users able to see all policies, all tasks, all documents etc. A normal user to see only his/her policies and claims. Explore this by looging in as a user who is part of the group "admin-group" and by logging as a normal user who is not part of this group.
3. Policy and Claim data retrieved from the respective System of Records (Elasticsearch in this example)
4. Use of Google Material Design components - Menu bar, Badges(Task Inbox Number), Icons (Side bar), Toggles (Open/Completed task), Buttons etc
5. Use of ADF components to show the data from System of Records
	Eg: ADF Datatable component used to list the claims and processes fetched from Elasticsearch
	Eg: Use of APS Forms outside of process to show data from Elasticsearch.
6. Mashup data and show documents and process data in a single page – 
	Eg: claim details, policy details pages.
7. Custom logic to populate forms.
	Eg: Option to clone a claim using the data from a previous claim. 
    Eg: Option to start a claim from a policy details page copying data from policy such as policy number, contact details etc
