package com.activiti.extension.bean;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.api.GroupService;
import com.activiti.service.runtime.PermissionService;

@Service
@Primary
public class CustomPermissionService extends PermissionService {

	/*
	 * The default Permission Service may not be suitable for all the customers.
	 * In this case users can leverage the power of Spring (check the Primary
	 * annotation above) and override the default PermissionService in Alfresco
	 * Process Service. In this example, I am giving all the users in a group
	 * called "watchlist-group" all kinds of access. As you can see it is quite
	 * easy to customize and meet your requirements
	 */

	@Autowired
	GroupService groupService;

	public static final String WATCHLIST_GROUP_NAME = "watchlist-group";

	@Override
	public boolean hasReadPermissionOnProcessInstance(User user, String processInstanceId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasReadPermissionOnProcessInstance(user, processInstanceId);
	}

	@Override
	public HistoricTaskInstance validateReadPermissionOnTask(User user, String taskId) {
		if (isUserInWatchListGroup(user)) {
			HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
					.taskId(taskId);
			HistoricTaskInstance task = historicTaskInstanceQuery.singleResult();
			return task;
		} else {
			return super.validateReadPermissionOnTask(user, taskId);
		}
	}

	@Override
	public boolean hasReadPermissionOnProcessInstance(User user, HistoricProcessInstance historicProcessInstance,
			String processInstanceId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasReadPermissionOnProcessInstance(user, historicProcessInstance, processInstanceId);
	}

	@Override
	public boolean hasReadPermissionOnRuntimeApp(User user, Long appId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasReadPermissionOnRuntimeApp(user, appId);
	}

	@Override
	public boolean hasReadPermissionOnProcessDefinition(User user, String processDefinitionId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasReadPermissionOnProcessDefinition(user, processDefinitionId);
	}

	@Override
	public boolean hasReadPermissionOnProcessDefinition(User user, ProcessDefinition processDefinition) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasReadPermissionOnProcessDefinition(user, processDefinition);
	}

	@Override
	public boolean hasWritePermissionOnRelatedContent(User user, RelatedContent content) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.hasWritePermissionOnRelatedContent(user, content);
	}

	@Override
	public boolean canAddRelatedContentToProcessInstance(User user, String processInstanceId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.canAddRelatedContentToProcessInstance(user, processInstanceId);
	}

	@Override
	public boolean canAddRelatedContentToTask(User user, String taskId) {
		return isUserInWatchListGroup(user) ? isUserInWatchListGroup(user)
				: super.canAddRelatedContentToTask(user, taskId);
	}

	@Override
	public boolean canDownloadContent(User currentUserObject, RelatedContent content) {
		return isUserInWatchListGroup(currentUserObject) ? isUserInWatchListGroup(currentUserObject)
				: super.canDownloadContent(currentUserObject, content);
	}

	private boolean isUserInWatchListGroup(User user) {
		Group group = groupService.getGroupByNameAndTenantId(WATCHLIST_GROUP_NAME, 1L).get(0);
		return groupService.isUserInGroup(group, user);
	}

}
