package com.activiti.extension.bean;

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("asyncReqReplyServiceTask")
public class AsyncReqReplyServiceTask implements SignallableActivityBehavior {


	protected static final Logger logger = LoggerFactory.getLogger(AsyncReqReplyServiceTask.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(ActivityExecution execution) throws Exception {
		logger.info(execution.getId());
	}

	@Override
	public void signal(ActivityExecution execution, String signalEvent, Object signalData) throws Exception {
		PvmTransition transition = execution.getActivity().getOutgoingTransitions().get(0);
		execution.take( transition);
	}

}
