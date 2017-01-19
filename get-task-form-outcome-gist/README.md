## A delegate class showing how to obtain task form outcome from submitted forms.
Originally answered at https://community.alfresco.com/message/806310-re-how-to-access-the-outcome-of-a-review-task-in-tasklistener

The task form outcome is saved as a process variable form<formid>outcome. However in scenarios like multi-instance, form re-use etc this variable could get overwritten every time. So it is better not to rely on the process variable if you want to find the selected outcome on each task. You would be better of using the 
submittedFormService bean which will use the outcome data from each submittedForms associated with the individual tasks. 