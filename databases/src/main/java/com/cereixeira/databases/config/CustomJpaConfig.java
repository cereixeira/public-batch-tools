package com.cereixeira.databases.config;

import com.cereixeira.databases.constants.DatabasesConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  basePackages = DatabasesConstants.PACKAGE_BASE,
  //basePackageClasses = Execution.class,
  entityManagerFactoryRef = "inputEntityManagerFactory",
  transactionManagerRef = "inputTransactionManager"
)
public class CustomJpaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean inputEntityManagerFactory(@Qualifier(DatabasesConstants.BEAN_CUSTOM_DS) DataSource dataSource) {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        //jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan(DatabasesConstants.PACKAGE_REPOSITORY);

        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager inputTransactionManager(
      @Qualifier("inputEntityManagerFactory") LocalContainerEntityManagerFactoryBean inputEntityManagerFactory) {
        return new JpaTransactionManager(inputEntityManagerFactory.getObject());
    }
}
