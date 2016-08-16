package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;

@Slf4j
@Component
public class CacheManager {

	public void writeCache(String trackId, byte[] fileContent) {
		log.info("Writing cache - " + trackId);
	}
}
