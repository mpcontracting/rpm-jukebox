package uk.co.mpcontracting.rpmjukebox;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.scene.image.Image;
import uk.co.mpcontracting.rpmjukebox.component.ProgressSplashScreen;
import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class RpmJukeboxTest extends AbstractTest {

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

    @Test(expected = IllegalStateException.class)
    public void shouldRunMain() {
        RpmJukebox.main(null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRunMainWhereLoggingFileAlreadyExists() throws Exception {
        File loggingFile = new File(RpmJukebox.getConfigDirectory(), "logback.xml");
        loggingFile.createNewFile();

        RpmJukebox.main(null);
    }

    @Test
    public void shouldReturnEmptyListForDefaultIcons() {
        Collection<Image> defaultIcons = rpmJukebox.loadDefaultIcons();

        assertThat("Default icons should be empty", defaultIcons, hasSize(0));
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
    public void shouldNotCallStartOnApplicationManagerInBeforeInitialViewWithNullApplicationManager() {
        reset(mockContext);

        rpmJukebox.beforeInitialView(null, mockContext);

        verify(mockApplicationManager, never()).start(any());
    }

    @Test
    public void shouldCallStopOnApplicationManagerInStop() throws Exception {
        ReflectionTestUtils.setField(rpmJukebox, "context", mockContext);
        rpmJukebox.stop();

        verify(mockApplicationManager, times(1)).stop();
    }

    @Test
    public void shouldNotCallStopOnApplicationManagerInStopNullContext() throws Exception {
        ReflectionTestUtils.setField(rpmJukebox, "context", null);
        rpmJukebox.stop();

        verify(mockApplicationManager, never()).stop();
    }

    @Test
    public void shouldNotCallStopOnApplicationManagerInStopWithNullApplicationManager() throws Exception {
        reset(mockContext);
        ReflectionTestUtils.setField(rpmJukebox, "context", mockContext);

        rpmJukebox.stop();

        verify(mockApplicationManager, never()).stop();
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
