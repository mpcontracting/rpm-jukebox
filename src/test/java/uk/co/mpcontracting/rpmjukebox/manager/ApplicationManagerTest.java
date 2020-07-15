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
    private Environment environment;

    @Mock
    private RpmJukebox rpmJukebox;

    @Mock
    private EventManager eventManager;

    @Mock
    private MessageManager messageManager;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private SearchManager searchManager;

    @Mock
    private MediaManager mediaManager;

    @Mock
    private JettyServer jettyServer;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private Stage stage;

    @Mock
    private ObservableList<Image> observableList;

    private final ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

    private ApplicationManager underTest;

    @Before
    public void setup() {
        underTest = new ApplicationManager(threadRunner, environment, rpmJukebox, messageManager);
        underTest.wireJettyServer(jettyServer);
        underTest.wireSettingsManager(settingsManager);
        underTest.wireSearchManager(searchManager);
        underTest.wireMediaManager(mediaManager);
        underTest.setApplicationContext(applicationContext);

        setField(underTest, "eventManager", eventManager);

        when(stage.getIcons()).thenReturn(observableList);
    }

    @Test
    @SneakyThrows
    public void shouldInitialise() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"default"});

        underTest.initialise();

        verify(searchManager, times(1)).initialise();
        verify(settingsManager, times(1)).loadUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotInitialiseForTestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        underTest.initialise();

        verify(searchManager, never()).initialise();
        verify(settingsManager, never()).loadUserSettings();
    }

    @Test
    public void shouldInitialiseStageOnOsx() {
        when(settingsManager.getOsType()).thenReturn(OsType.OSX);
        when(messageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(underTest, "stage", null);
        setField(underTest, "isInitialised", false);

        underTest.start(stage);

        assertThat(getField(underTest, "stage")).isNotNull();
        assertThat((boolean) getField(underTest, "isInitialised")).isTrue();

        verify(stage, times(1)).setTitle("WindowTitle");
        verify(stage, never()).getIcons();
        verify(settingsManager, times(1)).loadWindowSettings(stage);
        verify(stage, times(1)).show();
        verify(stage, times(1)).requestFocus();
        verify(eventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnWindows() {
        when(settingsManager.getOsType()).thenReturn(OsType.WINDOWS);
        when(messageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(underTest, "stage", null);
        setField(underTest, "isInitialised", false);

        underTest.start(stage);

        assertThat(getField(underTest, "stage")).isNotNull();
        assertThat((boolean) getField(underTest, "isInitialised")).isTrue();

        verify(stage, times(1)).setTitle("WindowTitle");
        verify(stage, times(1)).getIcons();
        verify(settingsManager, times(1)).loadWindowSettings(stage);
        verify(stage, times(1)).show();
        verify(stage, times(1)).requestFocus();
        verify(eventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnLinux() {
        when(settingsManager.getOsType()).thenReturn(OsType.LINUX);
        when(messageManager.getMessage(MESSAGE_WINDOW_TITLE)).thenReturn("WindowTitle");
        setField(underTest, "stage", null);
        setField(underTest, "isInitialised", false);

        underTest.start(stage);

        assertThat(getField(underTest, "stage")).isNotNull();
        assertThat((boolean) getField(underTest, "isInitialised")).isTrue();

        verify(stage, times(1)).setTitle("WindowTitle");
        verify(stage, never()).getIcons();
        verify(settingsManager, times(1)).loadWindowSettings(stage);
        verify(stage, times(1)).show();
        verify(stage, times(1)).requestFocus();
        verify(eventManager, times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    @SneakyThrows
    public void shouldStopApplication() {
        setField(underTest, "stage", stage);
        setField(underTest, "isInitialised", true);

        underTest.stop();

        verify(mediaManager, times(1)).cleanUpResources();
        verify(searchManager, times(1)).shutdown();
        verify(settingsManager, times(1)).saveWindowSettings(stage);
        verify(settingsManager, times(1)).saveUserSettings();
        verify(jettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldStopApplicationWhenNotInitialised() {
        setField(underTest, "stage", stage);
        setField(underTest, "isInitialised", false);

        underTest.stop();

        verify(mediaManager, times(1)).cleanUpResources();
        verify(searchManager, times(1)).shutdown();
        verify(settingsManager, never()).saveWindowSettings(stage);
        verify(settingsManager, never()).saveUserSettings();
        verify(jettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldStopApplicationWhenExceptionThrown() {
        doThrow(new RuntimeException("ApplicationManagerTest.shouldStopApplicationWhenExceptionThrown()"))
                .when(jettyServer).stop();

        setField(underTest, "stage", stage);
        setField(underTest, "isInitialised", false);

        underTest.stop();

        verify(mediaManager, times(1)).cleanUpResources();
        verify(searchManager, times(1)).shutdown();
        verify(settingsManager, never()).saveWindowSettings(stage);
        verify(settingsManager, never()).saveUserSettings();
        verify(jettyServer, times(1)).stop();
    }

    @Test
    @SneakyThrows
    public void shouldShutdownApplication() {
        underTest.shutdown();

        verify(applicationContext, times(1)).close();
    }
}
