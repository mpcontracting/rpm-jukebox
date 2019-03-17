package uk.co.mpcontracting.rpmjukebox.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfiguration {

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public ThreadRunner threadRunner(ExecutorService executorService) {
        return new ThreadRunner(executorService);
    }
}
