package in.dataman.donation.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "in.dataman.donation.transrepository", 
        entityManagerFactoryRef = "TransactionEntityManagerFactory", 
        transactionManagerRef = "TransactionManager")
public class TransactionDataSourceConfig {
	
    @Autowired
    private ExternalConfig externalConfig;

    @Autowired
    @Qualifier("companyDataSource")
    private DataSource companyDataSource;

    @Bean(name = "TransactionDataSource")
    DataSource TransactionDataSource() {
        // Fetch the dynamic database name using a utility function
        String dynamicDatabaseName = fetchDynamicDatabaseName();

        if (dynamicDatabaseName == null || dynamicDatabaseName.isEmpty()) {
            throw new RuntimeException("Failed to fetch the dynamic database name");
        }

        

        return DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + externalConfig.getSqlHostName() + ":" + externalConfig.getSqlPort()
                        + ";databaseName=" + dynamicDatabaseName
                        + ";encrypt=true;trustServerCertificate=true")
                .username(externalConfig.getSqlUser())
                .password(externalConfig.getSqlPassword())
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .build();
    }

    private String fetchDynamicDatabaseName() {
        String sql = "SELECT cmp.centralData_Path " +
                     "FROM productLicensedto plt " +
                     "LEFT JOIN company cmp ON cmp.comp_Code = plt.latestCompCode ";
        try (Connection connection = companyDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("centralData_Path"); // Assuming centralData_Path contains the database name
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch dynamic database name for DLM Transaction", e);
        }
        return null;
    }
    @Bean(name = "TransactionEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean dlmTransactionEntityManagerFactory(
            @Qualifier("TransactionDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("in.dataman.donation.transentity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaProperties(jpaProperties());
        return factory;
    }

    @Primary
    @Bean(name = "TransactionManager")
    PlatformTransactionManager TransactionManager(
            @Qualifier("TransactionEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        
        // Add additional properties here as needed
        return properties;
    }

    @Bean(name = "TransactionJdbcTemplate")
    JdbcTemplate dlmTransactionJdbcTemplate(@Qualifier("TransactionDataSource") DataSource TransactionDataSource) {
        return new JdbcTemplate(TransactionDataSource);
    }
	
    @Bean
    @Qualifier("TransactionJdbcTemplate")
    SimpleJdbcCall simpleJdbcCall(@Qualifier("TransactionDataSource") DataSource dataSource) {
        return new SimpleJdbcCall(dataSource);
    }
	
}
