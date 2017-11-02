## A sample project showing how to deploy and run drools in Alfresco Process Services (tested in version 1.7.0)

## How to run this example

1. mvn clean package will generate a jar (a pre-packaged version is available in this project aps-enable-drools-1.0.0-SNAPSHOT.jar).
2. Place the jar file in webapps/activiti-app/WEB-INF/lib
3. Deploy ProcessWithDrools.zip via Admin App (activiti-admin)
4. Start the process instance using the below engine API
POST - http://<host>:<port>/activiti-app/api/runtime/process-instances?tenantId=tenant_1
BODY - {"processDefinitionKey":"DroolsTest", "tenantId": "tenant_1", "variables": [ {"name":"custTypCD", "value":"Retail"}]}

#### Note:
1. You will also need to copy all the below jars to lib folder
    1. antlr-3.3.jar
    2. antlr-runtime-3.3.jar
    3. drools-compiler-5.4.0.Final.jar
    4. drools-core-5.4.0.Final.jar
    5. knowledge-api-5.4.0.Final.jar
    6. knowledge-internal-api-5.4.0.Final.jar
    7. mvel2-2.1.0.drools16.jar
    8. mvel2-2.2.6.Final.jar - REMOVE THIS JAR (I had to remove this from lib folder - some class loader issues with the above library)
