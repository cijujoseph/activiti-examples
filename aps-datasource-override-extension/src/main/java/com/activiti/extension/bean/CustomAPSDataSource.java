package com.activiti.extension.bean;

import com.activiti.api.datasource.DataSourceBuilderOverride;
import com.activiti.domain.idm.Tenant;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Component("customAPSDataSource")
public class CustomAPSDataSource implements DataSourceBuilderOverride {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CustomAPSDataSource.class);

	@Autowired
	protected Environment env;

	@Override
	public DataSource createDataSource(Tenant tenant, PropertyResolver propertyResolver) {
		return createDataSource(null, propertyResolver);
	}

	@Override
	public DataSource createDataSource(PropertyResolver propertyResolver) {
		log.info("inside datasource override");
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try {
			ds.setDriverClass("org.h2.Driver");
		} catch (PropertyVetoException e) {
			log.error("Error setting jdbc driver class");
			return null;
		}
		ds.setJdbcUrl("jdbc:h2:tcp://localhost/activiti-app-db-override");
		ds.setUser("alfresco");
		ds.setPassword("alfresco");
		log.info("datasource override executed successfully");
		return ds;
	}
}
