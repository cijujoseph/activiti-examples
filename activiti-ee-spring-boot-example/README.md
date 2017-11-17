# How to run
Place a valid licence file in src/main/resources

mvn clean spring-boot:run

Import the postman collection and test via the domain apis! All the activiti rest endpoints are also available if you want to test those. 

To configure APS Admin, do the following in admin
1. Admin -> Configuration -> Create new cluster -> Use the cluster name, user/pw as specified in the activiti-cluster.properties src/main/resources

2. Then edit Rest Endpoint Configuration in admin using the information from application.properties. The REST API username/pw is admin/password which is configured through the main class Application.java in the project. Once it is done, monitor the processes using admin



