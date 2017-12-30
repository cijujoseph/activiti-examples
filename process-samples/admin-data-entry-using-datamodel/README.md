
## An example showing how to quickly create an admin process which can be used for data entry (inserts/updates into database tables using data models)

*	Step 1:	Create a data source configuration via Alfresco Process Service UI->Identity Management->Tenants->Data sources so that data models can be configured to use this connection to connect to the database. For more details on data models please refer [User Guide](https://docs.alfresco.com/process-services1.7/topics/data_models.html)

* 	Step 2: Create the following table in your database to run this example.
```
CREATE TABLE CARS ( VIN VARCHAR(50) PRIMARY KEY, BRAND VARCHAR(50), COLOR VARCHAR(50), YEAR NUMBER );
Note - If using MySQL the attribute 'NUMBER' is not valid.  The following CREATE statement will work
CREATE TABLE cars (vin VARCHAR(50) PRIMARY KEY, brand VARCHAR(50), color VARCHAR(50), year INT);
```
*	Step 3: Import "admin-data-entry-using-datamodel.zip" Alfresco Process Service UI -> App Designer -> Apps ->Import. 

* 	Step 4: Publish and run the app by entering data in the start form. The data should be saved to database upon form submit
