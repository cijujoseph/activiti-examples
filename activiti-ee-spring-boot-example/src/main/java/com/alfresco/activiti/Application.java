package com.alfresco.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

		return new InitializingBean() {
			public void afterPropertiesSet() throws Exception {
				
				if (identityService.createUserQuery().userId("admin").list().size() == 0) {
					User admin = identityService.newUser("admin");
					admin.setPassword("password");
					identityService.saveUser(admin);
				}
				
			}
		};
	}

}
