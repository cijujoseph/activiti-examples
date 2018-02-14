## Helper project to obtain the DDL scripts of Alfresco Process Services powered by Activiti.

The SQL DDL scripts of Core Activiti Engine database tables can be found in the activiti-engine-X.X.X.X.jar (org/activiti/db/create|upgrade|drop) which is available in activiti-app/WEB-INF/lib folder

[Liquibase](http://www.liquibase.org/) is used to evolve the additional enterprise only part Alfresco Process Services DB schema. The following section will help generate those DDL scripts

This is a simple tool that can be used to generate DB SQL scripts for your database using the liquibase change log files availabile in the Alfresco Process Services (Activiti Enterprise).  The Liquibase change log files can be seen in activiti-app-data-X.X.X.jar (Activiti-app schema), activiti-admin/WEB-INF/classes/META-INF folder in the activiti-admin.war(Activiti Admin schema). 

Steps: 
1.	Update the pom.xml with an appropriate database driver dependency
2. Update the liquibase.properties with the correct details
3.	Copy the required file into the root folder, change the liquibase.properties file to point to the correct xml file. Run the following command:
4. Run one of the following commands
	1. `mvn liquibase:updateSQL` - This generates the SQL scripts in the target folder which can then be executed against the database.
	2. `mvn liquibase:update` - This command will execute the scripts against the database. In some cases DBAs would like to review the scripts first and execute it after review. In such a scenario, run the first command, review the scripts and then run second command to create/update the database.
