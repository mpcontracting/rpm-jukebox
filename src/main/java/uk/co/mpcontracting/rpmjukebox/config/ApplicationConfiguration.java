package uk.co.mpcontracting.rpmjukebox.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfiguration {

  @Bean
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }
}
