package com.activiti.extension.bean;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.service.api.GroupService;
import com.activiti.service.runtime.events.RuntimeEventListener;

import static org.activiti.engine.task.IdentityLinkType.*;

@Component("taskWatchlistListener")
public class TaskWatchlistListener implements RuntimeEventListener {

	/*
	 * A helper class which will add a group as a participant to all the tasks
	 * that are created in the system (this is done using an event listener at
	 * task creation time). This means that all the users in this particular
	 * group will be able see all the tasks in the system in their task list
	 */

	@Autowired
	private TaskService taskService;

	@Autowired
	private GroupService groupService;

	public static final String WATCHLIST_GROUP_NAME = "watchlist-group";

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		Long groupId = groupService.getGroupByNameAndTenantId(WATCHLIST_GROUP_NAME, 1L).get(0).getId();
		if (event.getType().equals(ActivitiEventType.TASK_CREATED)) {
			Task task = (Task) ((ActivitiEntityEvent) event).getEntity();
			taskService.addGroupIdentityLink(task.getId(), Long.toString(groupId), PARTICIPANT);
		}

	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
