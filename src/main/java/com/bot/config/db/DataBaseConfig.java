package com.bot.config.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.bot.mappers")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataBaseConfig {
    @Value("${spring.datasource.driver-class-name}")
    String driver;
    @Value("${spring.datasource.url}")
    String url;
    @Value("${spring.datasource.username}")
    String userName;
    @Value("${spring.datasource.password}")
    String pass;
    @Value("${spring.datasource.maximumPoolSize}")
    Integer maxPollSize;
    @Value("${spring.datasource.connectionTimeout}")
    Integer connectionTimeout;
    @Value("${spring.datasource.minimumIdle}")
    Integer minimumIdle;
    @Value("${spring.datasource.maximumPoolSize}")
    Integer maximumPoolSize;
    @Value("${spring.datasource.leakDetectionThreshold}")
    Integer leakDetectionThreshold;
    @Value("${spring.datasource.poolName}")
    String poolName;
    @Value("${spring.datasource.maxLifetime}")
    Integer maxLifetime;
    @Value("${spring.datasource.idleTimeout}")
    Integer idleTimeout;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        var sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        sessionFactory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        sessionFactory.setTransactionFactory(new SpringManagedTransactionFactory());
        return sessionFactory.getObject();
    }

    @Bean("TX_MANAGER_BEAN")
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(getDataSource());
    }

    @Bean
    public DataSource getDataSource() {
        final var config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(userName);
        config.setPassword(pass);
        config.setMaximumPoolSize(maxPollSize);
        config.setConnectionTimeout(connectionTimeout);
        config.setMinimumIdle(minimumIdle);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setPoolName(poolName);
        config.setMaxLifetime(maxLifetime);
        config.setIdleTimeout(idleTimeout);
        config.setRegisterMbeans(false);

        return new HikariDataSource(config);
    }
}
