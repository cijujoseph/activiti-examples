# APS Purging Project Ideas

### Delete Core Engine data

###### Option 1 - Using SQL Scripts
* For deleting the core engine's historical data, you could write SQL scripts to delete data from tables with prefix ACT_HI_*

###### Option 2 - Using APIs
* Query for historical process instances, historyService.createHistoricProcessInstanceQuery().finishedBefore(endDate).list()
* Iterate over each of them and do a delete historyService.deleteHistoricProcessInstance(processInstanceId). This will delete all the associated tasks, activities, variables, identitylinks, comments etc from history table.
* If you use standalone tasks (not attached to process instance), you may want to cleanup that too in a similar way using APIs as mentioned above.
* historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(endDate).list()
* history.deleteHistoricTaskInstance(taskId);

### Cleaning up additional tables

Some of the other tables you may want to clean up are:
* ACT_EVT_LOG (if events are turned on)
* PROCESSED_ACTIVITI_EVENTS (provided you are using Alfresco Process Services' event processing feature)
* RUNTIME_DECISION_AUDIT (Alfresco Process Services' DMN feature)
* SAVED_FORM (only if you are using Alfresco Process Services' forms)
* SUBMITTED_FORM (only if you are using Alfresco Process Services' forms)
There are no Delete APIs over the above entities. You can cleanup those tables using SQL scripts. All these tables have a ProcessInstanceId/TaskId column which you can use to make sure that you are not deleting any entries where there are valid entries in ACT_HI_PROCINST and/or ACT_HI_TASKINST tables.

### Alfresco Process Services' Content Cleanup
The following two tables holds process content metadata information and the actual local content is store on file system.
* CONTENT_RENDITION
* RELATED_CONTENT

###### Option 1 - Using APIs
* There is an API (JAVA as well as REST) that will let you delete both the metadata (from DB) and the actual content from file system.

###### Option 2 - Using SQL Scripts & Shell Scripts
* If you want to delete these entries using plain SQL scripts & files using shell scripts, you may want to convert all the STORE_ID data in your RELATED_CONTENT table to actual file system paths before you delete the data base entries. There is a utility class called PathConverter.java in the Alfresco Process Services java dependencies that will let you convert the Store ID to Path. I have an example here. Once you have all the file paths, you can delete them using shell scripts.

* RELATED_CONTENT table contains a ProcessInstanceId/TaskId column which you can use to make sure that you are not deleting any entries where there are valid entries in ACT_HI_PROCINST and/or ACT_HI_TASKINST tables.

### Cleanup of Dev environment
MODEL_HISTORY table is another table you may consider cleaning up in your development environment. But this should not be big in UAT/Prod etc
Another idea is to implement all the above logic as a process and let it run every day (using a timer start event). 
