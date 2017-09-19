package org.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;

public class ServiceTask1 implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("ServiceTask1 Execution");
	}

}
