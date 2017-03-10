package uk.co.mpcontracting.rpmdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PropertySource("classpath:datascraper-${spring.profiles.active}.properties")
public class DataScraperService {
	
	public static void main(String[] args) {
		if (System.getProperty("spring.profiles.active") == null) {
			System.setProperty("spring.profiles.active", "live");
		}

		SpringApplication.run(DataScraperService.class, args);
	}
}
