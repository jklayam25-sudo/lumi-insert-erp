package lumi.insert.app;

import java.util.Collections;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("resource")
@ActiveProfiles("test")
public abstract class TestContainerTest { 

    public static PostgreSQLContainer<?> container;

    static {
        container = new PostgreSQLContainer<>("postgres:18.1-alpine")
            .withTmpFs(Collections.singletonMap("/var/lib/postgresql", "rw"));
        container.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", container::getJdbcUrl);
        registry.add("spring.flyway.user", container::getUsername);
        registry.add("spring.flyway.password", container::getPassword);

        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);

        registry.add("spring.datasource.core.url", container::getJdbcUrl);
        registry.add("spring.datasource.core.username", container::getUsername);
        registry.add("spring.datasource.core.password", container::getPassword);

        registry.add("spring.datasource.activitycore.url", container::getJdbcUrl);
        registry.add("spring.datasource.activitycore.username", container::getUsername);
        registry.add("spring.datasource.activitycore.password", container::getPassword);
    }
}
