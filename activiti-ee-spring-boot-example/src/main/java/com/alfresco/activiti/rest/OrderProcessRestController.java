package com.alfresco.activiti.rest;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.alfresco.activiti.model.Approval;
import com.alfresco.activiti.model.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
public class OrderProcessRestController {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/orders/create-order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void createOrder(@RequestBody Order data) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		// Convert POJO to Map
		Map<String, Object> vars = mapper.convertValue(data, new TypeReference<Map<String, Object>>() {
		});
		
		if (vars.get("orderNumber") == null) {
			throw new Exception("Order Number Not Present");
		}
		runtimeService.startProcessInstanceByKey("CreateOrder", (String) vars.get("orderNumber"), vars);
	}

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/orders/{orderNumber}/approve", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void approve(@PathVariable String orderNumber, @RequestBody Approval data) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		// Convert POJO to Map
		Map<String, Object> vars = mapper.convertValue(data, new TypeReference<Map<String, Object>>() {
		});

		vars.put("approved", true);
		List<Task> taskList = taskService.createTaskQuery().processInstanceBusinessKey(orderNumber)
				.taskDefinitionKey("approve-request").list();
		if (taskList.size() > 0) {
			taskService.complete(taskList.get(0).getId(), vars);

		} else {
			throw new Exception("Order Not Found");
		}

	}

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/orders/{orderNumber}/reject", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void reject(@PathVariable String orderNumber, @RequestBody Approval data) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		// Convert POJO to Map
		Map<String, Object> vars = mapper.convertValue(data, new TypeReference<Map<String, Object>>() {
		});

		vars.put("approved", false);
		List<Task> taskList = taskService.createTaskQuery().processInstanceBusinessKey(orderNumber)
				.taskDefinitionKey("approve-request").list();
		if (taskList.size() > 0) {
			taskService.complete(taskList.get(0).getId(), vars);
		} else {
			throw new Exception("Order Not Found");
		}
	}

	@ResponseStatus(value = HttpStatus.OK)
	@RequestMapping(value = "/orders/{orderNumber}/resubmit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void resubmit(@PathVariable String orderNumber, @RequestBody Order data) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		// Convert POJO to Map
		Map<String, Object> vars = mapper.convertValue(data, new TypeReference<Map<String, Object>>() {
		});

		List<Task> taskList = taskService.createTaskQuery().processInstanceBusinessKey(orderNumber)
				.taskDefinitionKey("resubmit-request").list();
		if (taskList.size() > 0) {
			taskService.complete(taskList.get(0).getId(), vars);
		} else {
			throw new Exception("Order Not Found");
		}
	}

}