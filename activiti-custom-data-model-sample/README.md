#### Use of custom Datamodel to get a business process talking to Elasticsearch. The embedded Elasticsearch instance which comes with Alfresco Process Services (Enterprise Activiti) is used as the data source in this example. 

This project is also explained in a blog which is available at https://community.alfresco.com/community/bpm/blog/2017/04/12/business-data-integration-made-easy-with-data-models

## Overview of the processes used in this example

1. Process 1 - An insurance policy process using which a user creates a policy. As part of the process the policy data is stored in an external source via Datamodel. In this example external source is Elasticsearch.
2. Process 2- A claims process using which a user makes a claim. In this process, the Datamodel component is used extensively to do the following
	1. Fetch policy details and display the policy details to a reviewer
	2. Validate the data entered by user against what is recorded in the policy using Decision Tables and prompt user to update the policy details.
	3. Create a claim entry in the external claims system. In this example external source is Elasticsearch.

## Configuration Steps

1. Enable the HTTP endpoint my modifying the following two properties in the activiti-app.properties file.
	1.	elastic-search.enable.http=true
	2.	elastic-search.enable.http.cors=true
2. Deploy the "activiti-custom-data-model-sample-1.0.0-SNAPSHOT.jar" available in this project to activiti-app/WEB-INF/lib
3. Import the "InsuranceDemoApp.zip" in this project into your instance and publish the app.

## Run & Observe Steps

1. Start the "Create Policy" process by populating the values in the start form.
2. Start the "Claims Process" process by using the "Policy ID" used in the previous "Create Policy" process. When creating a claims process, enter a Customer Email and Contact Number different from what has been used in the "Create Policy" process. This will create a task for the user to update the policy with the correct information.
3. Now go through the "Review Claim" task form to see the policyDetails.customerName, policyDetails.contactNumber, policyDetails.customerEmail which are fetched from the Elasticsearch via Datamodel. If there is an "Update Policy" task along with the "Review Claim", complete this task prior to completing the "Review Claim" by updating the Email and Contact Number with a new value. Now go to the "Review Claim" task and see the value you updated via the "Update Policy" shown in fields policyDetails.contactNumber, policyDetails.customerEmail.
4. As part of the claims process I'm also creating a claims entry into Elasticsearch using the Datamodel.

### If you would like to see the data directly from the external data source, use the following URLS.
1.	http://localhost:9200/insuranceindex/policyevent/policyId
2.	http://localhost:9200/insuranceindex/claimevent/claimId entered in 'Review Claim' task

