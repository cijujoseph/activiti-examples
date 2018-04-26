package com.activiti.extension.bean;

import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.RestFieldRepresentation;
import com.activiti.model.runtime.FormValueRepresentation;
import com.activiti.service.runtime.AlfrescoTaskFormService;

@Service
@Primary
public class CustomAlfrescoTaskFormService extends AlfrescoTaskFormService {

	@Autowired
	HistoryService historyService;

	@Override
	public List<FormValueRepresentation> getRestFieldValues(String taskId, String field,
			Map<String, String> submittedHeaders) {

		HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		List<FormValueRepresentation> values = null;
		FormFieldRepresentation selectedField = getFormFieldFromTaskForm(taskId, field);

		if (selectedField instanceof RestFieldRepresentation) {
			RestFieldRepresentation restField = (RestFieldRepresentation) selectedField;
			if (restField.getRestUrl().contains("soapproxy") && restField.getRestUrl().contains("lookupModel=true")) {
				
				String urlAdditionalParams = "&runtimeFormId="+getTaskForm(task.getFormKey()).getId();
				if(!restField.getRestUrl().contains("fieldId=")){
					urlAdditionalParams = urlAdditionalParams + "&fieldId="+selectedField.getId();
				}
				if (restField.getEndpoint() != null) {
					values = restFieldService.invokeRestUrlWithEndpointConfig(restField.getId(),
							restField.getRestUrl() + "&runtimeFormId=" + urlAdditionalParams,
							restField.getRestResponsePath(), restField.getRestIdProperty(),
							restField.getRestLabelProperty(), restField.getEndpoint(), restField.requestHeadersAsMap(),
							submittedHeaders);
				} else {
					values = restFieldService.invokeRestUrl(selectedField.getId(),
							selectedField.getRestUrl() + urlAdditionalParams,
							restField.getRestResponsePath(), selectedField.getRestIdProperty(),
							selectedField.getRestLabelProperty());
				}
				return values;
			}

		}
		return super.getRestFieldValues(taskId, field, submittedHeaders);
	}

}
