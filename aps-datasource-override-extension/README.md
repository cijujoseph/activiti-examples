An example showing how to override the default datasource creation in APS.

1. Stop APS
2. Update the override class in this example CustomAPSDataSource.java with your override logic
3. `mvn clean package` will create a jar named aps-datasource-override-extension-1.0-SNAPSHOT.jar (sample jar available in the root of this project)
4. Place the jar in webapps/activiti-app/WEB-INF/lib directory and start APS
