
## A REST extension over database based Data Models in Alfresco Process Services

### Configuration & available REST APIs 


To configure this extension it is easy, all you have to do is place the aps-database-datamodel-rest-extension-1.0.0-SNAPSHOT.jar file in webapps/activiti-app/WEB-INF/lib directory and you will have the following REST APIs available over your database based data model definitions.

### Available REST APIs 

1. GET http://\<aps-host-name\>\:<aps-port-number\>/activiti-app/api/enterprise/custom-api/datamodels/\<data-model-id\>/entities/\<entity-name\>. This API will get all the entries from the database based on your data model definition. Basically this is the equivalent of "SELECT * FROM TABLE_NAME". One of the common use cases of this API is to show all the entries in a table as a drop down in a form field! Another use case is to display all the entries in a form table.

2. GET http://\<aps-host-name\>:\<aps-port-number\>/activiti-app/api/enterprise/custom-api/datamodels/\<data-model-id\>/entities/\<entity-name\>/\<keyValue\>?keyName=\<keyName\>. This is to get/lookup a row in the table using a where clause. Equivalent of "SELECT * FROM TABLE_NAME WHERE \<key name(DataModel Entity Attribute Name)\>=\<key value\>".

Since the APIs are under '/activiti-app/api/enterprise', they are secured using HTTP Basic Auth by default.
