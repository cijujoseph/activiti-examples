### Deployment of Alfresco Process Services (APS) on Pivotal Cloud Foundry
This is an example project showing how to repackage the activiti-app.war of Alfresco Process Services powered by Activiti with customer specific libraries, configurations and also with  PCF specific configurations. This is tested with APS v1.8 on PCFDev v0.28.0

### PCFDev Installation & Commands
Install PCFDev by following the instructions in this [page](https://pivotal.io/pcf-dev)
Useful commands to start, stop and destroy an environment are
`cf dev start`
`cf dev stop`
`cf dev destroy` respectively

### Deployment approach
With PCF Pivotal Container Service(PKS) going GA a few days ago and while the Activiti team is working on making Activiti a true [cloud native platform](https://activiti.gitbooks.io/activiti-7-developers-guide/) that can run natively on PCF PKS, this example project is created to help customers who wish to deploy current version of Alfresco Process Services in their PCF PAS environment. Since Alfresco Process Service is packaged & distributed as a war file, the approach here is to repackage the war file with PCF specific configurations & deploy to PCF.

#### Prerequisites & project set up
* You must have valid license files to run the software. Place the two license files named `Aspose.Total.Java.lic` & `activiti.lic` in `src/main/resources` folder of this project. 
> Important: The name of the files matters, so make sure they are the same as above. Make sure name is correct!

* You must have access to Alfresco's Enterprise Nexus repository to download the war file & dependencies. Customers will be able to request access via support/customer portal
> Important: Please check the pom.xml for the repository configuration and you must have a corresponding configuration in your maven configuration settings.xml

* The war is repackaged using [maven-war-plugin](https://maven.apache.org/plugins/maven-war-plugin/index.html). As explained in the [usage](https://maven.apache.org/plugins/maven-war-plugin/usage.html) page, all customer specific configuration overrides must be placed in the respective folder structure. You can explode the war distribution and explore various configuration files in activiti-app to understand this better. All the configurations are explained in great detail at [APS Admin Guide](https://docs.alfresco.com/process-services1.8/topics/adminGuide.html) as well. 
	* All non-java resources that needs to go under `activiti-app/WEB-INF/classes` should be kept `src/main/resources` in the required folder structure.  
	* Additional custom java classes can be kept under `src/main/java`.
	* All webapp specific customizations should be kept under `src/main/webapp`. 
	* Additional jar files that needs to go under `activiti-app/WEB-INF/lib` can be added to the pom.xml

#### Build the war file
In this project, I am customizing/overriding three files which are `src/main/resources/activiti/whitelisted-scripts.conf`, `log4j.properties`, `META-INF/activiti-app/activiti-app.properties`. You can repackage the war using the command `mvn clean compile war:war`.

#### PCF specific configurations and considerations
Before we go into the deployment steps, let's take a look at some of the PCF specific configuratios and considerations.

###### APS Database Configuration	(activiti-app.properties)
When running in PCF you have a few options to configure the database for APS which are:

* Use a Service from the marketplace (`cf marketplace`) & bind to the app. I have tested this approach by creating a MySQL service in PCFDev & binding APS to the MySQL service and it just works! As far as I understand, a service bound to an app will take precedence over a data source configured via activiti-app.properties
* Create a User-Provided Service Instance (`cf cups`) & configure the data source properties   in the activiti-app.properties with attributes from VCAP_SERVICES in the format `${vcap.services.yourservicename.*}`
* Use an External database and configure via environment variables. In this project I went for this option. Please check the activiti-app.properties file for more details.

###### Event Processing & Elasticsearch Configuration (activiti-app.properties)
Out of the box (OOTB), APS publishes all the analytics data into Elasticsearch. APS v1.8 added a new capability to publish analytics data to a newer version of Elastic using the recommended Java REST Client of Elasticsearch. For more details on this new configuration, please checkout this [blog](https://community.alfresco.com/community/bpm/blog/2018/02/06/alfresco-process-services-18-available-now#jive_content_id_New_Elasticsearch_REST_Client). When running APS on PCF, it is recommended to utilize this feature and publish the events to an external elastic search, should you choose to use the ELK stack. 
> However when you use this new feature, OOTB Analytics feature of the product no-longer works. Hence it is better to disable it in the configuration using the property. If you are wondering why the analytics module is not updated to work with this new REST client configuration, the future direction is to develop advanced analytics & reporting capabilities in this new module called [Alfresco Search Services & Analytics](https://community.alfresco.com/people/harry.peek@alfresco.com/blog/2017/11/08/alfresco-search-services-analytics-roadmap-update-aw2017).

On running & configuring elastic search itself:
1. Run externally outside PCF and configure activiti-app.properties via a User-Provided Service Instance OR using environment variables attached to APS app.
2. Create & configure to use the service if there is a service available in the marketplace
3. Run in PKS and connect to it.
4. Run externally and use a Service Broker approach

> If you really need Analytics Module in APS when running on PCF, you will need to run the unsupported 1.7.3 Elasticsearch externally and configure the properties file as a `client` as explained in [docs](https://docs.alfresco.com/process-services1.8/topics/elasticsearch_configuration.html). Embedded (Elasticsearch server running embedded within Process Services) is not recommended when running on PCF. 

> If you wish to direct the analytics data to another data source, please read this [page](https://docs.alfresco.com/process-services1.8/topics/process_engine_event_listeners.html) and the PCF configuration itself will depend on your choice!

###### Outbound Email Server Configuration (activiti-app.properties)
For sending outbound emails, APS would require a valid EMail Server configured in the environment. Please refer the [docs](https://docs.alfresco.com/process-services1.8/topics/emailServerConfiguration.html) for more details on various email configuration options. I am assuming that PCF Platform will already have outbound email servers configured for notifications. If this assumption is true & if those properties are available via some services, activiti-app.properties can be mapped using those properties. In this example project, I'm using environment variables.
###### Shared Content Store Configuration (activiti-app.properties)
Alfresco Process Services enables you to upload content, such as attaching a file to a task or a form. This content is stored on a disk and this location must be a shared storage area for the nodes to access the content. As you can see from the properties file, the property `contentstorage.fs.rootFolder` is used to map the shared path. For storing contents, APS can be configured either with Amazon S3 OR Shared Storage.

* If it is S3, it can be configured using any S3 services available in PCF Marketplace or by creating a User-Provided Service Instance or via Environment Variables.

* If not using S3, leveraging the NFS Volume Service in PCF, the storage path can be mapped. In this example, I am using the [local-volume](https://github.com/cloudfoundry/local-volume-release) service. You can create local-volume service using the command `cf create-service local-volume free-local-disk contentstore` and use the property `${vcap.services.contentstore.volume_mounts[0].container_dir}` as a value to the property `contentstorage.fs.rootFolder`
###### LDAP Configuration (activiti-app.properties)
###### LDAP Sync (activiti-app.properties)
###### External Content Platform integrations(activiti-app.properties)
Content integration to cloud platforms such as Alfresco Cloud, Box, Google Drive can be configured either using a User-Provided Service Instance (`cf cups`) approach or using environment variables. For the sake of simplicity I excluded those property mappings from the activiti-app.properties in this example. However you can add them in by referring this [page](https://docs.alfresco.com/process-services1.8/topics/integration_with_external_systems.html). For a complete list of available properties, you can also look at the default activiti-app.properties file which is available in the activiti-app.war distribution from Alfresco.
###### Logging Configuration (log4j.properties)
The log4j.properties file in this example project is configured with standard STDOUT as explained in the [PCF Application Logging docs](https://docs.cloudfoundry.org/devguide/deploy-apps/streaming-logs.html). This will enable the Process Services logging to appear in PCF logs (both via the console as well as using commands such as `cf logs APP_NAME`). While developing solutions & extensions on APS, please ensure that all the logging is implemented via log4j.
###### Tomcat Configuration (environment variable)
https://github.com/cloudfoundry/java-buildpack


### Deployment

```
cf create-service local-volume free-local-disk contentstore
cd <this project root>
cf push -f ./manifest.yml
```
##### Some URLs to validate a successful deployment
* **APS OOTB UI**: http://aps.local.pcfdev.io/activiti-app/workspace
* **ADF based Process Workspace**: http://aps.local.pcfdev.io/activiti-app/workspace
* **API Explorer**: http://aps.local.pcfdev.io/activiti-app/api-explorer.html


