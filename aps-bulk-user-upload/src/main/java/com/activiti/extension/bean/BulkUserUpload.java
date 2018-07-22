package com.activiti.extension.bean;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.activiti.domain.idm.AccountType;
import com.activiti.domain.idm.User;
import com.activiti.domain.idm.UserStatus;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.service.api.UserService;
import com.activiti.service.runtime.RelatedContentService;
import com.activiti.service.runtime.RelatedContentStreamProvider;
import com.alfresco.aps.bulk.user.upload.model.UserModel;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

@Component("bulkUserUpload")
public class BulkUserUpload implements JavaDelegate {

	@Autowired
	protected UserService userService;

	@Autowired
	protected RelatedContentService relatedContentService;
	
	@Autowired
    protected PasswordEncoder passwordEncoder;

	@Autowired
	protected RelatedContentStreamProvider relatedContentStreamProvider;

	protected static final Logger logger = LoggerFactory.getLogger(BulkUserUpload.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		RelatedContent csvFile = getFieldContent(execution.getProcessInstanceId(), "csv").get(0);
		
		Reader reader = new InputStreamReader(relatedContentStreamProvider.getContentStream(csvFile));

		CsvToBean csv = new CsvToBean();

		CSVParser parser = new CSVParserBuilder()
					.withSeparator(',')
					.withIgnoreQuotations(true)
					.build();

		List list = csv.parse(setColumMapping(), reader);

		for (Object object : list) {
			UserModel user = (UserModel) object;
			User apsUser = new User();
			if(user.getUserId()!=null){
				String encodedPassword = user.getPassword() != null ? passwordEncoder.encode(user.getPassword()) : null;
				if(userService.findUserByExternalId(user.getUserId()) == null) {
					apsUser.setPassword(encodedPassword);
					apsUser.setFirstName(user.getFirstName());
					apsUser.setLastName(user.getLastName());
					apsUser.setEmail(user.getEmail());
					apsUser.setCompany(user.getCompany());
					apsUser.setExternalId(user.getUserId());
					apsUser.setStatus(UserStatus.ACTIVE);
					apsUser.setAccountType(AccountType.ENTERPRISE);
					apsUser.setLastUpdate(new Date());
					apsUser.setCreated(new Date());
					apsUser.setTenantId(1L);
					userService.save(apsUser);
				} else {
					Long userId = userService.findUserByExternalId(user.getUserId()).getId();
					userService.updateUser(userId, 
							user.getEmail(), user.getFirstName(), user.getLastName(), user.getCompany());
					userService.changePassword(userId, user.getPassword());
				}
			}
		}
		reader.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static HeaderColumnNameTranslateMappingStrategy setColumMapping() {
		HeaderColumnNameTranslateMappingStrategy strategy = new HeaderColumnNameTranslateMappingStrategy();
		strategy.setType(UserModel.class);

		Map columnMap = new HashMap();
		columnMap.put("Password", "password");
		columnMap.put("First Name", "firstName");
		columnMap.put("Last Name", "lastName");
		columnMap.put("E-mail Address", "email");
		columnMap.put("Company", "company");
		columnMap.put("User Name", "userId");
		strategy.setColumnMapping(columnMap);

		return strategy;
	}

	public List<RelatedContent> getFieldContent(String processInstanceId, String field) {

		List<RelatedContent> relatedContent = new ArrayList<RelatedContent>();
		Page<RelatedContent> page = null;
		int pageNumber = 0;
		try {
			while ((page == null) || (page.hasNext())) {
				page = relatedContentService.getFieldContentForProcessInstance(processInstanceId, field, 50,
						pageNumber);
				relatedContent.addAll(page.getContent());
				pageNumber++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return relatedContent;
	}

}
