package fi.hsl.transitlog.hfp.configuration;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.*;
import org.springframework.scheduling.concurrent.*;

@org.springframework.scheduling.annotation.EnableScheduling
@Configuration
public class EnableScheduling {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(6);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

}
