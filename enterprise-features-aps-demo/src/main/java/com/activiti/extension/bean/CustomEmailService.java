package com.activiti.extension.bean;

import java.util.List;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.activiti.domain.runtime.RelatedContent;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;

@Component("customEmailService")
public class CustomEmailService implements JavaDelegate {
	
	private Expression emailTemplate;

	private Expression toList;

	private Expression subject;

	private Expression contentField;
	
	private Expression includeAttachments;

	@Autowired
	ContentUtils contentUtils;

	@Autowired
	private EmailUtils emailUtils;

	protected static final Logger logger = LoggerFactory.getLogger(CustomEmailService.class);

	public void execute(DelegateExecution execution) throws Exception {

		String[] emailToList = { getExpressionValue(execution, toList) };
		
		List<RelatedContent> relatedContentList = null;
		if(Boolean.parseBoolean(getExpressionValue(execution, includeAttachments))){
			
			if (contentField != null) {
				relatedContentList = contentUtils.getFieldContent(execution.getProcessInstanceId(),
						contentField.getExpressionText());

			} else {
				relatedContentList = contentUtils.getContents(execution.getProcessInstanceId());
			}
		}		

		try {
			// send email
			emailUtils.sendEmail(emailToList, getExpressionValue(execution, subject),
					emailUtils.evaluateTemplate(getExpressionValue(execution, emailTemplate), execution.getVariables()),
					relatedContentList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Unable to send email");
		}
	}

	private String getExpressionValue(DelegateExecution execution, Expression field) {
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		Expression expression = expressionManager.createExpression(field.getExpressionText());
		return expression.getValue(execution).toString();
	}

}
