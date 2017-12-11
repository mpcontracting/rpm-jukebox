package uk.co.mpcontracting.rpmjukebox.test.support;

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
import javafx.event.EventType;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
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
	
	protected MouseEvent getMouseEvent(EventType<MouseEvent> eventType, MouseButton mouseButton, int numberOfClicks) {
	    return new MouseEvent(eventType, 0, 0, 0, 0, mouseButton, numberOfClicks, false, false, false, false, true, 
	        false, false, false, false, true, null);
	}
	
	protected ContextMenuEvent getContextMenuEvent(Object source) {
	    return new ContextMenuEvent(source, null, ContextMenuEvent.CONTEXT_MENU_REQUESTED, 0, 0, 0, 0, false, null);
	}
	
	protected DragEvent getDragEvent(EventType<DragEvent> eventType, Dragboard dragboard, TransferMode transferMode, Object gestureSource) {
	    return new DragEvent(eventType, dragboard, 0, 0, 0, 0, transferMode, gestureSource, null, null);
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
