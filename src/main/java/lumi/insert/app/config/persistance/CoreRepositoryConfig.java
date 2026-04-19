package lumi.insert.app.config.persistance;

import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuration for core database
 * <p>JPA Repositories divided by modules.</p> 
 * <p>This result in repository and entity manager run only at module lumi.insert.app.core</p> 
 * <p>Seperation due to perfomance reason.</p>
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "lumi.insert.app.core.repository",  
    entityManagerFactoryRef = "coreEntityManagerFactory",
    transactionManagerRef = "coreTransactionManager"
)
public class CoreRepositoryConfig {
    
    /**
     * Default EntityManager.<br>
     * Entity manager for core Entities.
     * @param builder
     * @param dataSource Use separated datasource
     * @return  
     */
    @Primary
    @Bean("coreEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean coreEntityManagerFactoryBean(EntityManagerFactoryBuilder builder, HikariDataSource dataSource){
        return builder
        .dataSource(dataSource)
        .packages("lumi.insert.app.core.entity")
        .persistenceUnit("primary")
        .build();
    }

    @Primary
    @Bean
    PlatformTransactionManager coreTransactionManager(EntityManagerFactory core){
        return new JpaTransactionManager(core);
    }
    
}
