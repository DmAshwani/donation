package in.dataman.donation.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = "in.dataman.donation.comrepository",
		entityManagerFactoryRef = "companyEntityManagerFactory",
		transactionManagerRef = "companyTransactionManager")
public class CompanyDataSourceConfig {
    @Autowired
    private ExternalConfig externalConfig;

    @Bean(name = "companyDataSource")
    DataSource companyDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .url("jdbc:sqlserver://" + externalConfig.getSqlHostName() + ":" + externalConfig.getSqlPort() + ";databaseName=" + externalConfig.getCompanyDb() + ";encrypt=true;trustServerCertificate=true")
                .username(externalConfig.getSqlUser())
                .password(externalConfig.getSqlPassword())
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .build();
    }
    
    
    @Bean(name = "companyJdbcTemplate")
    JdbcTemplate companyJdbcTemplate(@Qualifier("companyDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    
    
    @Bean(name = "companyEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean companyDlmEntityManagerFactory(
            @Qualifier("companyDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("in.dataman.donation.comentity"); // Adjust to where your entity classes are located
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter()); // Set Hibernate as JPA provider
        factory.setJpaProperties(jpaProperties()); // Set JPA properties
        return factory;
    }



    @Bean(name = "companyTransactionManager")
    JpaTransactionManager companyTransactionManager(
            @Qualifier("companyEntityManagerFactory")LocalContainerEntityManagerFactoryBean companyEntityManagerFactory
            ) {
        return new JpaTransactionManager(Objects.requireNonNull(companyEntityManagerFactory.getObject()));
    }

    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        return properties;
    }

    
}
