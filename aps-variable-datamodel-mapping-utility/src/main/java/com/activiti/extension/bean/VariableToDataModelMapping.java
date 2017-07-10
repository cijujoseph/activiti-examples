package com.activiti.extension.bean;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.activiti.bpmn.model.Process;
import org.springframework.stereotype.Component;

import com.activiti.editor.json.converter.BpmnJsonConverter;
import com.activiti.service.runtime.AlfrescoRuntimeDataModelService;
import com.activiti.variable.VariableEntityWrapper;

import static com.activiti.editor.constants.StencilConstants.*;

@Component("variableToDataModelMapping")
public class VariableToDataModelMapping implements JavaDelegate {

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private AlfrescoRuntimeDataModelService alfrescoRuntimeDataModelService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		BpmnModel bpmnModel = repositoryService.getBpmnModel(execution.getProcessDefinitionId());
		Process baseElement = bpmnModel.getMainProcess();
		VariableEntityWrapper variableEntityWrapper = null;
		List<ExtensionElement> variableExtensionElements = baseElement.getExtensionElements()
				.get(PROPERTY_EXECUTION_VARIABLES);
		if (CollectionUtils.isNotEmpty(variableExtensionElements)) {
			for (ExtensionElement variableElement : variableExtensionElements) {
				String mappedVariableName = variableElement.getAttributeValue(BpmnJsonConverter.MODELER_NAMESPACE,
						PROPERTY_EXECUTION_MAPPED_VARIABLENAME);

				variableEntityWrapper = alfrescoRuntimeDataModelService.handleExecutionVariableMappingForVariable(
						mappedVariableName, bpmnModel.getMainProcess(), execution.getVariables(),
						execution.getProcessInstanceId(), false, execution.getProcessDefinitionId());

				if (variableEntityWrapper != null) {
					execution.setVariable(mappedVariableName, variableEntityWrapper);
				}
			}
		}

	}

}
