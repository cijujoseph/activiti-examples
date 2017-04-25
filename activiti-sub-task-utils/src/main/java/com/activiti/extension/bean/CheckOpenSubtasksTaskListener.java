package com.activiti.extension.bean;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("checkOpenSubtasksTaskListener")
public class CheckOpenSubtasksTaskListener implements TaskListener {

	/**
	 * This task listener checks if there are any open sub tasks for the given task. 
	 * If yes, throw an exception and hence preventing the task completion.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(CheckOpenSubtasksTaskListener.class);

	@Autowired
	private HistoryService historyService;

	@Override
	public void notify(DelegateTask delegateTask) {

		long openSubtasks = historyService.createHistoricTaskInstanceQuery().taskParentTaskId(delegateTask.getId())
				.unfinished().count();
		if (openSubtasks > 0) {
			throw new RuntimeException(openSubtasks + " open subtasks/checklists found for " + delegateTask.getName());
		}

	}
}