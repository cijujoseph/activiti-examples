#### Helper class to support process variable to data model mapping definitions

At the time of writing (as of Alfresco Process Services 1.6.3 ) data models can be mapped and set only from a form field through the web modeler. However it is a common use case to map a process variable to a data model entity so that attributes in the mapped data model can be utilized in components such as process model, decision tables, forms etc..
Prior to Alfresco Process Services 1.6.3, there was a [defect](https://issues.alfresco.com/jira/browse/ACTIVITI-1039) preventing programmatic setting of data model entities from process variables.  With the release of 1.6.3 that issue is fixed and it is now possible to do the set a data model entity from process variable using the JAVA APIs. This utility class/bean (Task/Execution listener) is an implementation of the workaround mentioned in [this issue](https://issues.alfresco.com/jira/browse/ACTIVITI-1040).

## How to use this utility
* Deploy the jar file to APS lib.
* Define variable to data model mapping at process level. 
* Configure this bean (${variableToDataModelMapping}) to be executed after the variable has been set in the process. For example: if you set a variable in a script task, execute this class after the script task

Please import "variable-datamodel-app.zip" present in this project for an example usage. 




 

