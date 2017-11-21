package uk.co.mpcontracting.rpmjukebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import de.roskenet.jfxsupport.test.GuiTest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class AbstractTest extends GuiTest {

	static {
		System.setProperty("JAVAFX_HEADLESS", "true");
		System.setProperty("directory.config", ".rpmjukeboxtest");
		ReflectionTestUtils.setField(RpmJukebox.class, "configDirectory", 
				new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest"));
		Locale.setDefault(Locale.UK);
	}
	
	@Mock
	@Getter(AccessLevel.PROTECTED) private EventManager mockEventManager;
	private EventManager eventManager;
	
	@PostConstruct
	public void constructView() throws Exception {
        init(MainPanelView.class);
    }
	
	@Before
	public void abstractTestBefore() {
		RpmJukebox.getConfigDirectory().mkdirs();
		
		eventManager = EventManager.getInstance();
		ReflectionTestUtils.setField(EventManager.class, "instance", mockEventManager);
	}
	
	protected long getLocalDateTimeInMillis(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
	}
	
	protected long getDateTimeInMillis(int year, int month, int day, int hour, int minute) {
		return getLocalDateTimeInMillis(LocalDateTime.of(year, month, day, hour, minute));
	}
	
	protected File getTestResourceFile(String path) throws Exception {
		return new ClassPathResource(path).getFile();
	}
	
	protected String getTestResourceContent(String path) throws Exception {
		StringBuilder builder = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(getTestResourceFile(path)))) {
			reader.lines().forEach(line -> {
				builder.append(line);
				builder.append("\r\n");
			});
		}
		
		return builder.toString();
	}

	@After
	@SneakyThrows
	public void abstractTestCleanup() {
		ReflectionTestUtils.setField(EventManager.class, "instance", eventManager);
		
		File configDirectory = RpmJukebox.getConfigDirectory();
		
		if (configDirectory.exists()) {
			FileUtils.deleteDirectory(RpmJukebox.getConfigDirectory());
		}
	}
}
