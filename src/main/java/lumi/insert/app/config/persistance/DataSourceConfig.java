package lumi.insert.app.config.persistance;

import org.springframework.boot.context.properties.ConfigurationProperties; 
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; 

import com.zaxxer.hikari.HikariDataSource;

/**
 * Configuration for DB DataSource 
 * <p>Seperation due to perfomance reason.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.core")
    DataSourceProperties coreDataSource(){
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.core.hikari")
    HikariDataSource coreHikariDataSource(){
        return coreDataSource().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean 
    @ConfigurationProperties("spring.datasource.activitycore")
    DataSourceProperties activityDataSource(){
        return new DataSourceProperties();
    }

    @Bean(name = "activity-hikari-ds")
    @ConfigurationProperties("spring.datasource.activitycore.hikari")
    HikariDataSource activityHikariDataSource(){
        return coreDataSource().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

}
