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
@EnableJpaRepositories(basePackages = { "com.enliple.ar.jpa.db1.repository", "com.enliple.ar.jpa.db1.domain" },
		  transactionManagerRef   = "DB1_TransactionManager"
		, entityManagerFactoryRef = "DB1_EntityManagerFactory")
@Slf4j
public class Db1HikariCpDataSource {
	
	@Autowired
	Config config;
	
	@Bean(name = "DB1_DataSource")
	@Primary
	//@ConfigurationProperties(prefix = "db1.datasource")
	public DataSource dataSource() throws SQLException {
//		HikariDataSource dataSource = DataSourceBuilder.create()
//				.type(HikariDataSource.class)
//				.url(config.getMysqlUrl())
//				.username(config.getMysqlUser())
//				.password(config.getMysqlPassword())
//				.driverClassName(config.getMysqlDriver())
//				.build() ;		
		HikariConfig hikariconfig = new HikariConfig();
		hikariconfig.setJdbcUrl(config.getMysqlUrl());
		hikariconfig.setUsername(config.getMysqlUser());
		hikariconfig.setPassword(config.getMysqlPassword());
		
		hikariconfig.setConnectionTimeout(9000);//connect_timeout 10
		hikariconfig.setIdleTimeout(9000);//connect_timeout 10
		hikariconfig.setMaxLifetime(28000);//28800
		hikariconfig.setValidationTimeout(9000);//connect_timeout 10
		
		log.info("DB1_DataSource {} => {}/{}",config.getMysqlUrl(),config.getMysqlUser(),config.getMysqlPassword());
		HikariDataSource hikariDataSource = new HikariDataSource(hikariconfig);
		return hikariDataSource;
	}

	@Primary
	@Bean(name = "DB1_EntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean Db1EntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("DB1_DataSource") DataSource dataSource) {
		return builder.dataSource(dataSource)
				.packages("com.enliple.ar.jpa.db1.repository", "com.enliple.ar.jpa.db1.domain").build();

	}

	@Primary
	@Bean(name = "DB1_TransactionManager")
	public PlatformTransactionManager Db1TransactionManager(
			@Qualifier("DB1_EntityManagerFactory") EntityManagerFactory entityManagerFactory) {

		return new JpaTransactionManager(entityManagerFactory);

	}
}
