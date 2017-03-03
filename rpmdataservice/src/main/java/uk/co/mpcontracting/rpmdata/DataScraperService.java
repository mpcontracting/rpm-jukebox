package uk.co.mpcontracting.rpmdata;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataScraperService {

	public DataScraperService() {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			Scheduler scheduler = schedulerFactory.getScheduler();
	
			scheduler.start();
		} catch (Exception e) {
			log.error("Error starting Quartz scheduler", e);
		}
	}
	
	public static void main(String[] args) {
		SpringApplication.run(DataScraperService.class, args);
	}
}
