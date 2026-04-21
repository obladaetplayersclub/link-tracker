package backend.academy.linktracker.scrapper.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor linkUpdaterExecutor(LinkUpdaterProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getThreadCount());
        executor.setMaxPoolSize(props.getThreadCount());
        executor.setThreadNamePrefix("link-updater-");
        executor.initialize();
        return executor;
    }
}
