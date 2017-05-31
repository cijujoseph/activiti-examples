#### A simple end user facing ADF App built on top of a couple of business processes in Alfresco Process Services(APS). 


## Prerequisites
1. This example is built and tested against Alfresco Process Service Version 1.6.1

## Configuration Steps
1. Import one of the following two process apps into your APS instance and publish the app. Once the app is deployed, go to the landing page and make sure that the app is added to the landing page. Note down the app id from the APS UI.
	1. demo-resources/InsuranceProcessSuite.zip
				OR
	2. demo-resources/InsuranceProcessSuite_With-ACS-Integration.zip

2. Since I'm using a custom data model using the inbuilt Elasticsearch instance of APS, we need to enable the HTTP endpoint of APS Elasticsearch by modifying the following two properties in the activiti-app.properties file. For more details on this component and configuration, please refer [activiti-custom-data-model-sample](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-custom-data-model-sample)
	```
	elastic-search.enable.http=true
	elastic-search.enable.http.cors=true
	```
3. Deploy the "activiti-custom-data-model-sample-1.0.0-SNAPSHOT.jar" available in this project to activiti-app/WEB-INF/lib.

4. Import the following forms into your APS instance and note down the formids from APS UI.
	1. demo-resources/Insurance_Form_ADF.json
	2. demo-resources/Property Claim - ADF.json

5. Update the insurance-demo-adf-app/environments/environment.ts file using the IDs noted above and the IP addresses. Start the ADF App by executing the following commands.
	```
	cd insurance-demo-adf-app
	npm install
	npm start
	```
6. Access the ADF app by going to http://localhost:3000
7. Explore the access control implemented in the ADF app using the groups in APS. 
	1. Create a group called "admin-group" in APS->Identity Management->Organization and add a user in this group. Then do the following.
	2. Login as a user who is present in the admin-group which was created above. Observe the options available for an admin user.
	3. Logout
	4. Login as a user who is not present in the admin-group. Observer the options available for a non-admin user
	


