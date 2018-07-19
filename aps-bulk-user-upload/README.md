## Bulk User Upload Process

A utility process to create local users in bulk in APS from a CSV file format that is supported also supported by ACS. For more details around the CSV file format refer https://docs.alfresco.com/5.2/tasks/admintools-upload-users.html The start form of the process will prompt user to upload the CSV file. Once submitted, process will read the attached csv file and **create/updates** the users in APS local database. For a sample file please refer users.csv file in this project.

### Steps to deploy & run this project

1. Deploy aps-bulk-user-upload-1.0.0-SNAPSHOT.jar to the APS node - webapps/activiti-app/WEB-INF/lib
2. Restart APS
3. Import aps-bulk-user-upload-1.0.0-SNAPSHOT-app.zip into APS environment via APS UI -> App Designer -> Apps -> Import App
4. Publish the app to run processes in this app.

If you want to use the "User Name" field in your CSV file as the login ID instead of the default email address, please set **security.authentication.use-externalid=true** in activiti-app.properties.
