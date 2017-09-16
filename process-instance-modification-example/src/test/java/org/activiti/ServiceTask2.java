package org.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ServiceTask2 implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("ServiceTask2 Execution");
	}

}
