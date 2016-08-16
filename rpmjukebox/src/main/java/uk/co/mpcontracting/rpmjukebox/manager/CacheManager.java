package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.File;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class CacheManager implements InitializingBean, Constants {

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising CacheManager");
	}
	
	@Synchronized
	public File readCache(CacheType cacheType, String trackId) {
		log.info("Reading cache : Cache type - " + cacheType + ", Track ID - " + trackId);
		return null;
	}
	
	@Synchronized
	public void writeCache(CacheType cacheType, String trackId, byte[] fileContent) {
		log.info("Writing cache : Cache type - " + cacheType + ", Track ID - " + trackId);
	}
}
