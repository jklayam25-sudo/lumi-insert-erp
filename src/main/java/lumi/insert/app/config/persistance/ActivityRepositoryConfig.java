package lumi.insert.app.config.persistance;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration for logging action database
 * <p>JPA Repositories divided by modules.</p> 
 * <p>This result in repository and entity manager run only at module lumi.insert.app.activitycore</p> 
 * <p>Seperation due to perfomance reason.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "lumi.insert.app.activitycore.repository",  
    entityManagerFactoryRef = "activityEntityManagerFactory",
    transactionManagerRef = "activityTransactionManager"
)
public class ActivityRepositoryConfig {
     
    /**
     * Entity manager for activitycore Entities.
     * @param builder
     * @param dataSource Use separated datasource
     * @return  
     */
    @Bean("activityEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean activityEntityManagerFactoryBean(EntityManagerFactoryBuilder builder, @Qualifier("activity-hikari-ds") HikariDataSource dataSource){
        return builder
        .dataSource(dataSource)
        .packages("lumi.insert.app.activitycore.entity")
        .persistenceUnit("activity")
        .build();
    }
 
    @Bean("activityTransactionManager")
    PlatformTransactionManager activityTransactionManager(@Qualifier("activityEntityManagerFactory") EntityManagerFactory core){
        return new JpaTransactionManager(core);
    }
    
}
