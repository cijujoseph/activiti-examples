package com.activiti.extension.bean;

import com.activiti.service.api.GroupService;
import com.activiti.service.runtime.events.RuntimeEventListener;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.activiti.engine.task.IdentityLinkType.PARTICIPANT;

@Component("processWatchlistListener")
public class ProcessWatchlistListener implements RuntimeEventListener {

	/*
	 * A helper class which will add a group as a participant to all the processes
	 * that are created in the system (this is done using an event listener at
	 * process instance creation time). This means that all the users in this particular
	 * group will be able see all the tasks in the system in their task list
	 */

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private GroupService groupService;

	public static final String PROCESS_WATCHLIST_GROUP_NAME = "process-watchlist-group";

	@Override
	public boolean isFailOnException() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		Long groupId = groupService.getGroupByNameAndTenantId(PROCESS_WATCHLIST_GROUP_NAME, 1L).get(0).getId();
		if (event.getType().equals(ActivitiEventType.PROCESS_STARTED)) {
			System.out.println("event logged");
			ProcessInstance pi = (ProcessInstance) ((ActivitiEntityEvent) event).getEntity();
			runtimeService.addGroupIdentityLink(pi.getId(), Long.toString(groupId), PARTICIPANT);
		}

	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
