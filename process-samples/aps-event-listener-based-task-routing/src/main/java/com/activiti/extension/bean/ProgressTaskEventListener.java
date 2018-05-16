package com.activiti.extension.bean;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("progressTaskEventListener")
public class ProgressTaskEventListener implements ActivitiEventListener {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event.getType().equals(ActivitiEventType.TASK_ASSIGNED)) {
            Task task = (Task) ((ActivitiEntityEvent) event).getEntity();
            if(runtimeService.getVariable(event.getExecutionId(), "form"+task.getFormKey()+"outcome").equals("Reject")) {
                if (!runtimeService.getVariable(event.getExecutionId(), "returnbackto").equals(task.getTaskDefinitionKey())) {
                    taskService.complete(task.getId());
                }
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
