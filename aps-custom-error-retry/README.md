## A sample "Fibonacci backoff retry" extension for Alfresco Process Services (tested in version 1.7.0) 

CustomJobRetryCmd.java in this project is a slightly modified version of https://github.com/Activiti/Activiti/blob/5.x/modules/activiti-engine/src/main/java/org/activiti/engine/impl/cmd/JobRetryCmd.java where I increased the default retry count to 10 instead of the default value of 3. Also, implemented a "Fibonacci backoff retry" strategy for managing the time between retries. For more details, check line# 61-78 in CustomJobRetryCmd.java

This is just an example, expand/modify to suite your needs. Eg: Following this example one can implement configuration(properties) driven error handling strategies to handle different types of errors like, retry 3 times if timeout error, no retry if invalid data error etc... 

## How to deploy to APS.

1. mvn clean package will generate a jar. If you just want to test this against your APS, a packaged jar (aps-custom-error-retry-1.0.0-SNAPSHOT.jar) is available in this project for you to use.
2. Place the jar file in webapps/activiti-app/WEB-INF/lib

