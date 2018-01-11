
## A BPMN stencil example showing how to make a generic component to save attachment(s) in a process to a location in file system

### Deploy the extension jar file

* Place the jar file (aps-save-to-filesystem-stencil-1.0-SNAPSHOT.jar) in webapps/activiti-app/WEB-INF/lib
* Restart Alfresco Process Services (if not configured to start automatically on classpath updates).

### Deploy the process app.
* Import the process app archive (Save File Example.zip) via App Designer -> Apps -> Import App
* Deploy the app by clicking on the "Publish" button.
* Inspect the process and check the configuration on the save stencil components to see various configuration options!
* Start the process by attaching files and verify that the files are saved to the configured locations



