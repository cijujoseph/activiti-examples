package com.activiti.extension.bean;

import java.util.ArrayList;
import java.util.List;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.repository.runtime.RelatedContentRepository;
import com.activiti.service.api.UserService;
import com.activiti.service.runtime.RelatedContentService;

@Component("copyAttachmentTaskListener")
public class CopyAttachmentTaskListener implements TaskListener {

/**
  * This task listener copies the "upload" field's attachments in parent
  * process instance to a new attachment field "upload1" in the subprocess
  * task.
  */
private static final long serialVersionUID = 1L;

private static final Logger log = LoggerFactory
   .getLogger(CopyAttachmentTaskListener.class);

@Autowired
private UserService userService;

@Autowired
private RelatedContentService relatedContentService;

@Autowired
private RelatedContentRepository relatedContentRepository;

@Override
public void notify(DelegateTask delegateTask) {
  List<RelatedContent> relatedContent = new ArrayList<RelatedContent>();
  Page<RelatedContent> page = null;

  while ((page == null) || (page.hasNext())) {
   page = relatedContentRepository.findAllByProcessInstanceIdAndField(
     (String) delegateTask.getExecution().getVariable(
       "superProcessInstanceId"), "supportingfiles", null);
   relatedContent.addAll(page.getContent());
  }
  for (RelatedContent rc : relatedContent) {
   // Create a new related content object
   RelatedContent newRelatedContentObject = relatedContentService
     .createRelatedContent(userService.getUser(new Long(
       delegateTask.getAssignee())), rc.getName(), rc
       .getSource(), rc.getSourceId(), delegateTask
       .getId(), delegateTask.getProcessInstanceId(),
       "supportingfiles", rc.getMimeType(), null, 0L);
   // Now copy all the existing values from original attachment
   newRelatedContentObject.setContentStoreId(rc.getContentStoreId());
   newRelatedContentObject
     .setContentAvailable(rc.isContentAvailable());   
   newRelatedContentObject.setContentSize(rc.getContentSize());
   newRelatedContentObject.setRelatedContent(rc.isRelatedContent());
   newRelatedContentObject.setField("supportingfiles");
   // Save
   relatedContentRepository.saveAndFlush(newRelatedContentObject);
  }
}
}
