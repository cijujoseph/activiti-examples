package org.activiti;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RetryProcessStepsUnitTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = { "org/activiti/test/RetryProcessSteps.bpmn20.xml" })
	public void testRetry() {
		System.out.println("**** Start testRetry() ****");
		// start process
		ProcessInstance processInstance = this.activitiRule.getRuntimeService()
				.startProcessInstanceByKey("RetryProcessSteps");

		assertNotNull(processInstance);

		assertEquals("receivetask1", processInstance.getActivityId());

		// Move execution back to service task 1 and retry
		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule
				.getProcessEngine().getProcessEngineConfiguration();

		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "servicetask1"));
		this.activitiRule.getRuntimeService()
				.signal(this.activitiRule.getRuntimeService().createExecutionQuery().singleResult().getId());

		assertEquals("receivetask1", processInstance.getActivityId());
		System.out.println("**** End testRetry() ****");

	}

	@Test
	@Deployment(resources = { "org/activiti/test/RetryProcessSteps.bpmn20.xml" })
	public void testRetryWithSkip() {
		System.out.println("**** Start testRetryWithSkip() ****");

		// start process
		ProcessInstance processInstance = this.activitiRule.getRuntimeService()
				.startProcessInstanceByKey("RetryProcessSteps");

		assertNotNull(processInstance);

		assertEquals("receivetask1", processInstance.getActivityId());

		// Move execution back to service task 1 and retry.
		ProcessEngineConfigurationImpl processEngineConfigurationImpl = (ProcessEngineConfigurationImpl) activitiRule
				.getProcessEngine().getProcessEngineConfiguration();

		CommandExecutor commandExecutor = processEngineConfigurationImpl.getCommandExecutor();
		commandExecutor.execute(new RestartInstanceActivitiCommand(processInstance.getId(), "servicetask1"));

		// Setting expression to skip service task 2 during retry
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
		variables.put("skipServiceTask2", true);

		this.activitiRule.getRuntimeService()
				.signal(this.activitiRule.getRuntimeService().createExecutionQuery().singleResult().getId(), variables);

		assertEquals("receivetask1", processInstance.getActivityId());

		System.out.println("**** End testRetryWithSkip() ****");

	}

}
