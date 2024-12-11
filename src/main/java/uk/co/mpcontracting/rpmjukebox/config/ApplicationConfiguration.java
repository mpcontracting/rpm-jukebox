package uk.co.mpcontracting.rpmjukebox.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfiguration {

  @Bean
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  @Bean
  public ThreadRunner threadRunner(ExecutorService executorService) {
    return new ThreadRunner(executorService);
  }
}
