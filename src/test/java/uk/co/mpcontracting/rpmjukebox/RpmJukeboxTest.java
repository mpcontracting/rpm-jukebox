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
    private ProgressSplashScreen mockSplashScreen;

    @Mock
    private ConfigurableApplicationContext mockContext;

    @Mock
    private ApplicationManager mockApplicationManager;

    @Before
    public void setup() {
        when(mockContext.getBean(ApplicationManager.class)).thenReturn(mockApplicationManager);
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
        rpmJukebox.beforeInitialView(null, mockContext);

        verify(mockApplicationManager, times(1)).start(any());
    }

    @Test
    public void shouldNotCallStartOnApplicationManagerInBeforeInitialViewWithNullContext() {
        rpmJukebox.beforeInitialView(null, null);

        verify(mockApplicationManager, never()).start(any());
    }

    @Test
    public void shouldUpdateSplashProgress() throws Exception {
        ReflectionTestUtils.setField(rpmJukebox, "splashScreen", mockSplashScreen);

        rpmJukebox.updateSplashProgress("Test message");

        // Wait for UI thread
        Thread.sleep(250);

        verify(mockSplashScreen, times(1)).updateProgress("Test message");
    }
}
