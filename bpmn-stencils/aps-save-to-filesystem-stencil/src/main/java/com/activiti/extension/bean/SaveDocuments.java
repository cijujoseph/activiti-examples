package com.activiti.extension.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.runtime.RelatedContentStreamProvider;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;

@Component("saveDocuments")
public class SaveDocuments implements JavaDelegate {

	@Autowired
	private RelatedContentStreamProvider relatedContentStreamProvider;

	private Expression path;

	private Expression contentField;

	@Autowired
	ContentUtils contentUtils;

	protected static final Logger logger = LoggerFactory.getLogger(SaveDocuments.class);

	public void execute(DelegateExecution execution) throws Exception {

		List<RelatedContent> relatedContentList = null;
		if (contentField != null) {
			relatedContentList = contentUtils.getFieldContent(execution.getProcessInstanceId(),
					contentField.getExpressionText());

		} else {
			relatedContentList = contentUtils.getContents(execution.getProcessInstanceId());
		}

		try {

			new File(getExpressionValue(execution, path) ).mkdir();

			for (RelatedContent rc : relatedContentList) {
				InputStream inputStream = relatedContentStreamProvider.getContentStream(rc);

				File file = new File(getExpressionValue(execution, path)  + "/" + rc.getName());
				file.setReadable(true);
				if (!file.exists())
					file.createNewFile();

				FileOutputStream outputStream = new FileOutputStream(file);
				IOUtils.copy(inputStream, outputStream);

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				logger.info("Done!");
				outputStream.close();
				inputStream.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String getExpressionValue(DelegateExecution execution, Expression field) {
		ExpressionManager expressionManager = Context.getProcessEngineConfiguration().getExpressionManager();
		Expression expression = expressionManager.createExpression(field.getExpressionText());
		return expression.getValue(execution).toString();
	}

}
