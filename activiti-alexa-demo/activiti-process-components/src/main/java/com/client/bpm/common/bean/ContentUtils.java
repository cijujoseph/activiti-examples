package com.client.bpm.common.bean;

import java.util.ArrayList;
import java.util.List;
import org.activiti.engine.delegate.BpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.runtime.RelatedContentService;

@Component("contentUtils")
public class ContentUtils {

	private static final Logger log = LoggerFactory.getLogger(ContentUtils.class);

	@Autowired
	private RelatedContentService relatedContentService;

	//get all documents in the process
	public List<RelatedContent> getContents(String processInstanceId) throws BpmnError {
		
		List<RelatedContent> relatedContent = new ArrayList<RelatedContent>();
		Page<RelatedContent> page = null;
		int pageNumber = 0;
		try {			
			while ((page == null) || (page.hasNext())) {
				page = relatedContentService
						.getAllFieldContentForProcessInstance(
								processInstanceId, 50,
								pageNumber);
				relatedContent.addAll(page.getContent());
				pageNumber++;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}	
		return relatedContent;
	}

}
