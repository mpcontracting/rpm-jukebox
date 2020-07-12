package uk.co.mpcontracting.rpmjukebox.manager;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.TestThreadRunner;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationManagerTest implements Constants {

    @Mock
    private Environment mockEnvironment;

    @Mock
    private RpmJukebox mockRpmJukebox;

    @Mock
    private EventManager mockEventManager;

    @Mock
    private MessageManager mockMessageManager;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private MediaManager mockMediaManager;

    @Mock
    private JettyServer mockJettyServer;

    @Mock
    private ConfigurableApplicationContext mockApplicationContext;

    @Mock
    private Stage mockStage;

    @Mock
    private ObservableList<Image> mockObservableList;

    private ApplicationManager applicationManager;
    private ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

    @Before
    public void setup() {
        applicationManager = new ApplicationManager(threadRunner, mockEnvironment, mockRpmJukebox, mockMessageManager);
        applicationManager.wireJettyServer(mockJettyServer);
        applicationManager.wireSettingsManager(mockSettingsManager);
        applicationManager.wireSearchManager(mockSearchManager);
        applicationManager.wireMediaManager(mockMediaManager);
        applicationManager.setApplicationContext(mockApplicationContext);

        setField(applicationManager, "eventManager", mockEventManager);

        when(mockStage.getIcons()).thenReturn(mockObservableList);
    }

    @Test
    @SneakyThrows
    public void shouldInitialise() {
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{"default"});

        applicationManager.initialise();

        verify(mockSearchManager, times(1)).initialise();
        verify(mockSettingsManager, times(1)).loadUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotInitialiseForTestProfile() {
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{"test"});

        applicationManager.initialise();

        verify(mockSearchManager, never()).initialise();
        verify(mockSettingsManager, never()).loadUserSettings();
    }

    @Test
    public void shouldInitialiseStageOnOsx() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);
        when(mockMessageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(applicationManager, "stage", null);
        setField(applicationManager, "isInitialised", false);

        applicationManager.start(mockStage);

        assertThat(getField(applicationManager, "stage")).isNotNull();
        assertThat((boolean) getField(applicationManager, "isInitialised")).isTrue();

        verify(mockStage, times(1)).setTitle("WindowTitle");
        verify(mockStage, never()).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(mockEventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnWindows() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.WINDOWS);
        when(mockMessageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(applicationManager, "stage", null);
        setField(applicationManager, "isInitialised", false);

        applicationManager.start(mockStage);

        assertThat(getField(applicationManager, "stage")).isNotNull();
        assertThat((boolean) getField(applicationManager, "isInitialised")).isTrue();

        verify(mockStage, times(1)).setTitle("WindowTitle");
        verify(mockStage, times(1)).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(mockEventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnLinux() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.LINUX);
        when(mockMessageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(applicationManager, "stage", null);
        setField(applicationManager, "isInitialised", false);

        applicationManager.start(mockStage);

        assertThat(getField(applicationManager, "stage")).isNotNull();
        assertThat((boolean) getField(applicationManager, "isInitialised")).isTrue();

        verify(mockStage, times(1)).setTitle("WindowTitle");
        verify(mockStage, never()).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(mockEventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    @SneakyThrows
    public void shouldStopApplication() {
        setField(applicationManager, "stage", mockStage);
        setField(applicationManager, "isInitialised", true);

        applicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, times(1)).saveWindowSettings(mockStage);
        verify(mockSettingsManager, times(1)).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldStopApplicationWhenNotInitialised() {
        setField(applicationManager, "stage", mockStage);
        setField(applicationManager, "isInitialised", false);

        applicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, never()).saveWindowSettings(mockStage);
        verify(mockSettingsManager, never()).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldStopApplicationWhenExceptionThrown() {
        doThrow(new RuntimeException("ApplicationManagerTest.shouldStopApplicationWhenExceptionThrown()"))
                .when(mockJettyServer).stop();

        setField(applicationManager, "stage", mockStage);
        setField(applicationManager, "isInitialised", false);

        applicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, never()).saveWindowSettings(mockStage);
        verify(mockSettingsManager, never()).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldShutdownApplication() {
        applicationManager.shutdown();

        verify(mockApplicationContext, times(1)).close();
    }
}
