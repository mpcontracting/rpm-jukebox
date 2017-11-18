package uk.co.mpcontracting.rpmjukebox;

import java.io.File;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import de.roskenet.jfxsupport.test.GuiTest;
import lombok.SneakyThrows;
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
	
	@PostConstruct
	public void constructView() throws Exception {
        init(MainPanelView.class);
    }

	@After
	@SneakyThrows
	public void afterCleanup() {
		File configDirectory = RpmJukebox.getConfigDirectory();
		
		if (configDirectory.exists()) {
			FileUtils.deleteDirectory(RpmJukebox.getConfigDirectory());
		}
	}
}
