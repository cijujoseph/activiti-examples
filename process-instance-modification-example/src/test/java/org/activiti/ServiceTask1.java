package org.activiti;

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;

public class ServiceTask1 implements SignallableActivityBehavior {

	private static final long serialVersionUID = 1L;

	public void execute(ActivityExecution execution) throws Exception {
		System.out.println("ServiceTask1 Execution");
		signal(execution,"signal","" );
	}

	public void signal(ActivityExecution execution, String signalEvent, Object signalData) throws Exception {
		if (signalEvent == null){
			System.out.println("--re-trying--");
			this.execute(execution);
		}
		PvmTransition transition = execution.getActivity().getOutgoingTransitions().get(0);
		execution.take( transition);
	}

}
