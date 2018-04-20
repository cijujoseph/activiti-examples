package com.activiti.extension.bean;

import java.util.List;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.runtime.Clock;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.StencilSetHistory;
import com.activiti.domain.runtime.Form;
import com.activiti.repository.editor.StencilSetHistoryRepository;
import com.activiti.repository.runtime.FormRepository;
import com.activiti.service.editor.AlfrescoFormService;

@Component("attachTaskForm")
public class AttachTaskForm implements TaskListener {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	protected FormRepository formRepository;

	@Autowired
	protected AlfrescoFormService formService;

	@Autowired
	protected Clock clock;

	@Autowired
	protected StencilSetHistoryRepository stencilSetHistoryRepository;

	@Override
	public void notify(DelegateTask task) {
		//Get the formId using the formKey set on Task
		Long formId = Long.parseLong((String) task.getVariable(task.getFormKey()));
		Model model = formService.getFormModel(formId);

		Form form = new Form();
		form.setName(model.getName());
		form.setDescription(model.getDescription());
		form.setCreated(clock.getCurrentTime());
		form.setModelId(formId);
		form.setDefinition(model.getModelEditorJson());
		form.setTenantId(model.getTenantId());
		if (model.getStencilSetId() != null && model.getStencilSetId() > 0) {
			List<StencilSetHistory> stencilHistoryList = stencilSetHistoryRepository
					.findByStencilSetIdOrderByIdDesc(model.getStencilSetId());
			if (CollectionUtils.isNotEmpty(stencilHistoryList)) {
				form.setStencilSetId(stencilHistoryList.get(0).getId());
			}

		}
		formRepository.save(form);
		// Overwrite the formKey with the new deployed formId
		task.setFormKey(form.getId().toString());

	}

}
