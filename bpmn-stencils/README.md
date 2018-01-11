## Custom BPMN Stencils

### General instructions to run the examples in this directory 

Unless specified otherwise in the respective folders, the zip archive files available in the example folders are “App” exports. To to use them, you will need to import them via App Designer (Kickstart App) -> Apps -> Import App. Once the “App” is successfully imported, the stencils along with an example process will also get imported which will make it easy for you to see these examples in action! Since the stencil implementations are in Java, you will need to deploy the extension jar file too...

### Deploy the extension jar file

* Place the jar file (aps-save-to-filesystem-stencil-1.0-SNAPSHOT.jar) in webapps/activiti-app/WEB-INF/lib
* Restart Alfresco Process Services (if not configured to re-start automatically on classpath updates).


### List of examples

1. Save attachment(s) in a process to a location in file system: **aps-save-to-filesystem-stencil**
2. Example showing a Custom ACS Integration built as a stencil: **aps-acs-integration-utils**
3. Example showing AWS SQS Publish Step built as a stencil: **aps-aws-sqs-extension**

