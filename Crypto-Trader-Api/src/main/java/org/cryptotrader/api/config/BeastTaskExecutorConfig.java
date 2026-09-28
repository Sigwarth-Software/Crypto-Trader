package org.cryptotrader.api.config;

//=================================-Imports-==================================
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Configuration for the beast task executor. */
@Configuration
@EnableAsync
@Profile("beast")
public class BeastTaskExecutorConfig {
    private static final String THREAD_NAME_PREFIX = "Beast-";
    //==============================-Beans-===================================

    //---------------------Thread-Pool-Task-Executor--------------------------
    /**
     * Bean for the beast thread pool task executor.
     * @return The beast thread pool task executor.
     */
    @Bean(name = "taskExecutor")
    public @NotNull TaskExecutor beastThreadPoolTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // TODO: Make these configurable values.
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}
