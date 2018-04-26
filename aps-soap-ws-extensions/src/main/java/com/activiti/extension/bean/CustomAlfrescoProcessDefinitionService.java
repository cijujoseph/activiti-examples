package com.activiti.extension.bean;

import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.RestFieldRepresentation;
import com.activiti.model.runtime.FormValueRepresentation;
import com.activiti.service.runtime.AlfrescoProcessDefinitionService;

@Service
@Primary
public class CustomAlfrescoProcessDefinitionService extends AlfrescoProcessDefinitionService {

	@Override
	public List<FormValueRepresentation> getRestFieldValues(String[] requestInfoArray,
			Map<String, String> submittedHeaders) {

		List<FormValueRepresentation> values = null;
		ProcessDefinition processDefinition = getProcessDefinitionFromRequest(requestInfoArray, false);
		FormFieldRepresentation selectedField = getFormFieldFromRequest(requestInfoArray, processDefinition, false);

		if (selectedField instanceof RestFieldRepresentation) {
			RestFieldRepresentation restField = (RestFieldRepresentation) selectedField;
			if (restField.getRestUrl().contains("soapproxy") && restField.getRestUrl().contains("lookupModel=true")) {
				String urlAdditionalParams = "&runtimeFormId="+getStartForm(processDefinition).getId();
				if(!restField.getRestUrl().contains("fieldId=")){
					urlAdditionalParams = urlAdditionalParams + "&fieldId="+selectedField.getId();
				}
				if (restField.getEndpoint() != null) {
					values = restFieldService.invokeRestUrlWithEndpointConfig(restField.getId(), restField.getRestUrl() + urlAdditionalParams,
							restField.getRestResponsePath(), restField.getRestIdProperty(),
							restField.getRestLabelProperty(), restField.getEndpoint(), restField.requestHeadersAsMap(),
							submittedHeaders);
				} else {
					values = restFieldService.invokeRestUrl(selectedField.getId(), selectedField.getRestUrl() + urlAdditionalParams,
							restField.getRestResponsePath(), selectedField.getRestIdProperty(),
							selectedField.getRestLabelProperty());
				}
				return values;
			}

		}
		return super.getRestFieldValues(requestInfoArray, submittedHeaders);
	}
}
