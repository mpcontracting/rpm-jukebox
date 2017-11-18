package uk.co.mpcontracting.rpmjukebox;

import java.io.File;

import javax.annotation.PostConstruct;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import de.roskenet.jfxsupport.test.GuiTest;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class AbstractTest extends GuiTest {

	static {
		System.setProperty("JAVAFX_HEADLESS", "true");
		ReflectionTestUtils.setField(RpmJukebox.class, "configDirectory", 
				new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest"));
	}
	
	@PostConstruct
	public void constructView() throws Exception {
        init(MainPanelView.class);
    }
}
