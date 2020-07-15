package uk.co.mpcontracting.rpmjukebox;

import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.component.ProgressSplashScreen;
import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import java.io.File;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RpmJukeboxTest extends AbstractGUITest {

    @Autowired
    private RpmJukebox rpmJukebox;

    @Mock
    private ProgressSplashScreen splashScreen;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private ApplicationManager applicationManager;

    @Before
    public void setup() {
        when(applicationContext.getBean(ApplicationManager.class)).thenReturn(applicationManager);
    }

    @Test
    public void shouldRunMain() {
        assertThatThrownBy(() -> RpmJukebox.main(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldRunMainWhereLoggingFileAlreadyExists() throws Exception {
        File loggingFile = new File(RpmJukebox.getConfigDirectory(), "logback.xml");
        loggingFile.createNewFile();

        assertThatThrownBy(() -> RpmJukebox.main(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldReturnEmptyListForDefaultIcons() {
        Collection<Image> defaultIcons = rpmJukebox.loadDefaultIcons();

        assertThat(defaultIcons).isEmpty();
    }

    @Test
    public void shouldCallStartOnApplicationManagerInBeforeInitialView() {
        rpmJukebox.beforeInitialView(null, applicationContext);

        verify(applicationManager, times(1)).start(any());
    }

    @Test
    public void shouldNotCallStartOnApplicationManagerInBeforeInitialViewWithNullContext() {
        rpmJukebox.beforeInitialView(null, null);

        verify(applicationManager, never()).start(any());
    }

    @Test
    public void shouldUpdateSplashProgress() throws Exception {
        ReflectionTestUtils.setField(rpmJukebox, "splashScreen", splashScreen);

        rpmJukebox.updateSplashProgress("Test message");

        // Wait for UI thread
        Thread.sleep(250);

        verify(splashScreen, times(1)).updateProgress("Test message");
    }
}
