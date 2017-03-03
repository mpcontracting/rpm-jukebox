package uk.co.mpcontracting.rpmdata;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:datascraper-${spring.profiles.active}.properties")
public class DataScraperConfig {

}
