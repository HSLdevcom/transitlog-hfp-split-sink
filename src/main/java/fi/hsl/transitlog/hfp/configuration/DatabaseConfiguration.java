package fi.hsl.transitlog.hfp.configuration;

import com.zaxxer.hikari.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;

import javax.sql.*;
import java.util.*;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
        basePackages = "fi.hsl.transitlog.hfp.domain.repositories",
        entityManagerFactoryRef = "devWriteEntityManager",
        transactionManagerRef = "devWriteTransactionManager"
)
@Profile(value = {"default", "dev"})
@EnableTransactionManagement
@EnableAsync
public class DatabaseConfiguration {
    @Autowired
    private Environment env;

    @Bean
    @Primary
    public PlatformTransactionManager devWriteTransactionManager() {
        JpaTransactionManager transactionManager
                = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                devWriteEntityManager().getObject());
        return transactionManager;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean devWriteEntityManager() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(devWriteDataSource());
        em.setPackagesToScan(
                "fi.hsl.transitlog.hfp.domain");

        HibernateJpaVendorAdapter vendorAdapter
                = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect",
                env.getProperty("hibernate.dialect"));
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.jdbc.batch_size", 5000);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.jdbc.time_zone", "UTC");
        properties.put("hibernate.jdbc.batch_versioned_data", true);
        properties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        properties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Primary
    public DataSource devWriteDataSource() {
        HikariDataSource dataSource
                = new HikariDataSource();
        dataSource.setDriverClassName(
                env.getProperty("jdbc.driverClassName"));
        dataSource.setJdbcUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.user"));
        dataSource.setPassword(env.getProperty("jdbc.pass"));
        return dataSource;
    }
}