package com.enliple.ar.config;


import com.enliple.ar.common.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "com.enliple.ar.jpa.db2.repository", "com.enliple.ar.jpa.db2.domain" },
		  transactionManagerRef   = "DB2_TransactionManager"
		, entityManagerFactoryRef = "DB2_EntityManagerFactory")
@Slf4j
public class Db2HikariCpDataSource {
	
	@Autowired
	Config config;
	
	@Bean(name = "DB2_DataSource")
	//@Primary
	public DataSource dataSource() throws SQLException {
//		HikariDataSource dataSource = DataSourceBuilder.create()
//				.type(HikariDataSource.class)
//				.url(config.getMysqlUrl())
//				.username(config.getMysqlUser())
//				.password(config.getMysqlPassword())
//				.driverClassName(config.getMysqlDriver())
//				.build() ;		
		HikariConfig hikariconfig = new HikariConfig();
		hikariconfig.setJdbcUrl(config.getMysqlUrlForJoblist());
		hikariconfig.setUsername(config.getMysqlUserForJoblist());
		hikariconfig.setPassword(config.getMysqlPasswordForJoblist());
		log.info("DB2_DataSource {} => {}/{}",config.getMysqlUrlForJoblist(),config.getMysqlUserForJoblist(),config.getMysqlPasswordForJoblist());
		HikariDataSource hikariDataSource = new HikariDataSource(hikariconfig);
		return hikariDataSource;
	}

	@Bean(name = "DB2_EntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean Db2EntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("DB2_DataSource") DataSource dataSource) {
		return builder.dataSource(dataSource)
				.packages("com.enliple.ar.jpa.db2.repository", "com.enliple.ar.jpa.db2.domain").build();

	}

	@Bean(name = "DB2_TransactionManager")
	public PlatformTransactionManager Db1TransactionManager(
			@Qualifier("DB2_EntityManagerFactory") EntityManagerFactory entityManagerFactory) {

		return new JpaTransactionManager(entityManagerFactory);

	}
}
