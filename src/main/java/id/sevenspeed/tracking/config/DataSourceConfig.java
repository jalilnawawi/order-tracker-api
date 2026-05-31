package id.sevenspeed.tracking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "dataSourceEntityManagerFactory",
        basePackages = {"id.sevenspeed.tracking.repository"},
        transactionManagerRef = "dataSourceTransactionManager"
)
public class DataSourceConfig {
    @Autowired
    Environment environment;

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty("spring.datasource.driver-class-name")));
        return dataSource;
    }

    @Bean(name = "dataSourceEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        bean.setPackagesToScan("id.sevenspeed.tracking.models.entity");
        bean.setPersistenceUnitName("dataSourceEntityManagerFactory");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(vendorAdapter);

        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.put("hibernate.show_sql", environment.getProperty("spring.jpa.show-sql"));
        bean.setJpaPropertyMap(properties);
        return bean;
    }

    @Bean(name = "dataSourceTransactionManager")
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean(name = "dataSourceJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
