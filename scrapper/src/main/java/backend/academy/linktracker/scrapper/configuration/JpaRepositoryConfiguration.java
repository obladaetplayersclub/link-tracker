package backend.academy.linktracker.scrapper.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "app.database-access-type", havingValue = "ORM")
@EnableJpaRepositories(basePackages = "backend.academy.linktracker.scrapper.repository.jpa")
public class JpaRepositoryConfiguration {}
