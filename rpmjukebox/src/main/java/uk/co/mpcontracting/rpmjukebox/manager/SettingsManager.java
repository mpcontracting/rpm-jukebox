package uk.co.mpcontracting.rpmjukebox.manager;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.annotation.Resource;
import uk.co.mpcontracting.ioc.factory.InitializingBean;

@Slf4j
@Component
public class SettingsManager implements InitializingBean {

	@Autowired
	private SearchManager searchManager;

	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MediaManager mediaManager;

	@Resource(location = "classpath:/rpm-jukebox.properties")
	private Properties rpmJukeboxProperties;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising SettingsManager");
	}
}
