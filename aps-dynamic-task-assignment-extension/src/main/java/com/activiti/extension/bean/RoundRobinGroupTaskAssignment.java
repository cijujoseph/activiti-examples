package com.activiti.extension.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.User;
import com.activiti.service.api.GroupService;
import com.activiti.service.api.UserService;

@Component("roundRobinGroupTaskAssignment")
public class RoundRobinGroupTaskAssignment implements TaskListener {

	private static final long serialVersionUID = 1L;

	@Autowired
	TaskService taskService;

	@Autowired
	UserService userService;

	@Autowired
	GroupService groupService;

	@Override
	public void notify(DelegateTask task) {
		Set<IdentityLink> candidates = task.getCandidates();
		ArrayList<String> userArray = new ArrayList<String>();
		for (IdentityLink idntyLink : candidates) {
			if (idntyLink.getGroupId() != null) {
				Set<User> users = groupService.getFunctionalGroup(Long.parseLong(idntyLink.getGroupId())).getUsers();
				for (User user : users) {
					if (!userArray.contains(user.getEmail())) {
						userArray.add(user.getEmail());
					}
				}
			}
		}
		Collections.sort(userArray);
		String taskDefKey = task.getTaskDefinitionKey();

		List<Task> taskList = taskService.createTaskQuery().taskDefinitionKey(taskDefKey).taskAssigneeLike("%")
				.orderByTaskCreateTime().desc().list();
		String nextAssignee;
		if (taskList.size() > 0) {
			String lastAssignee = userService.getUser(Long.parseLong(taskList.get(0).getAssignee())).getEmail();
			int lastAssigneeIndex = userArray.indexOf(lastAssignee);
			if (lastAssigneeIndex + 1 < userArray.size()) {
				nextAssignee = Long.toString(userService.findUserByEmail(userArray.get(lastAssigneeIndex + 1)).getId());
			} else {
				nextAssignee = Long.toString(userService.findUserByEmail(userArray.get(0)).getId());
			}
		} else {
			nextAssignee = Long.toString(userService.findUserByEmail(userArray.get(0)).getId());
		}
		task.setAssignee(nextAssignee);
		task.setName(task.getName() + " - AssigneeID: " + nextAssignee);

	}

}
