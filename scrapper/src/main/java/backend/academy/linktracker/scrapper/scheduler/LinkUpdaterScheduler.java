package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.service.LinkUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler implements SchedulingConfigurer {
    private final LinkUpdater linkUpdater;
    private final SchedulerProperties schedulerProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addFixedDelayTask(linkUpdater::update, schedulerProperties.getInterval());
    }
}
