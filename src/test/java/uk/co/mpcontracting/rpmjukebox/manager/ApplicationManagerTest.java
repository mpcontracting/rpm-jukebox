package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class ApplicationManagerTest extends AbstractTest implements Constants {

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private MessageManager messageManager;

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

    private ApplicationManager spyApplicationManager;

    @Before
    public void setup() {
        spyApplicationManager = spy(applicationManager);
        ReflectionTestUtils.setField(spyApplicationManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spyApplicationManager, "settingsManager", mockSettingsManager);
        ReflectionTestUtils.setField(spyApplicationManager, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(spyApplicationManager, "mediaManager", mockMediaManager);
        ReflectionTestUtils.setField(spyApplicationManager, "jettyServer", mockJettyServer);
        ReflectionTestUtils.setField(spyApplicationManager, "context", mockApplicationContext);

        when(mockStage.getIcons()).thenReturn(mockObservableList);
    }

    @Test
    public void shouldInitialise() throws Exception {
        Environment mockEnvironment = mock(Environment.class);
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] { "default" });

        ReflectionTestUtils.setField(spyApplicationManager, "environment", mockEnvironment);

        spyApplicationManager.initialise();

        verify(mockSearchManager, times(1)).initialise();
        verify(mockSettingsManager, times(1)).loadUserSettings();
    }

    @Test
    public void shouldNotInitialiseForTestProfile() throws Exception {
        Environment mockEnvironment = mock(Environment.class);
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] { "test" });

        ReflectionTestUtils.setField(spyApplicationManager, "environment", mockEnvironment);

        spyApplicationManager.initialise();

        verify(mockSearchManager, never()).initialise();
        verify(mockSettingsManager, never()).loadUserSettings();
    }

    @Test
    public void shouldInitialiseStageOnOsx() throws Exception {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);
        ReflectionTestUtils.setField(spyApplicationManager, "stage", null);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", false);

        spyApplicationManager.start(mockStage);

        boolean isInitialised = (Boolean)ReflectionTestUtils.getField(spyApplicationManager, "isInitialised");

        assertThat("Stage should not be null", ReflectionTestUtils.getField(spyApplicationManager, "stage"),
            notNullValue());
        assertThat("Is initialised should be true", isInitialised, equalTo(true));
        verify(mockStage, times(1)).setTitle(messageManager.getMessage(MESSAGE_WINDOW_TITLE));
        verify(mockStage, never()).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(getMockEventManager(), times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnWindows() throws Exception {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.WINDOWS);
        ReflectionTestUtils.setField(spyApplicationManager, "stage", null);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", false);

        spyApplicationManager.start(mockStage);

        boolean isInitialised = (Boolean)ReflectionTestUtils.getField(spyApplicationManager, "isInitialised");

        assertThat("Stage should not be null", ReflectionTestUtils.getField(spyApplicationManager, "stage"),
            notNullValue());
        assertThat("Is initialised should be true", isInitialised, equalTo(true));
        verify(mockStage, times(1)).setTitle(messageManager.getMessage(MESSAGE_WINDOW_TITLE));
        verify(mockStage, times(1)).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(getMockEventManager(), times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldInitialiseStageOnLinux() throws Exception {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.LINUX);
        ReflectionTestUtils.setField(spyApplicationManager, "stage", null);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", false);

        spyApplicationManager.start(mockStage);

        boolean isInitialised = (Boolean)ReflectionTestUtils.getField(spyApplicationManager, "isInitialised");

        assertThat("Stage should not be null", ReflectionTestUtils.getField(spyApplicationManager, "stage"),
            notNullValue());
        assertThat("Is initialised should be true", isInitialised, equalTo(true));
        verify(mockStage, times(1)).setTitle(messageManager.getMessage(MESSAGE_WINDOW_TITLE));
        verify(mockStage, never()).getIcons();
        verify(mockSettingsManager, times(1)).loadWindowSettings(mockStage);
        verify(mockStage, times(1)).show();
        verify(mockStage, times(1)).requestFocus();
        verify(getMockEventManager(), times(1)).fireEvent(Event.APPLICATION_INITIALISED);
    }

    @Test
    public void shouldStopApplication() throws Exception {
        ReflectionTestUtils.setField(spyApplicationManager, "stage", mockStage);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", true);

        spyApplicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, times(1)).saveWindowSettings(mockStage);
        verify(mockSettingsManager, times(1)).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    public void shouldStopApplicationWhenNotInitialised() throws Exception {
        ReflectionTestUtils.setField(spyApplicationManager, "stage", mockStage);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", false);

        spyApplicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, never()).saveWindowSettings(mockStage);
        verify(mockSettingsManager, never()).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    public void shouldStopApplicationWhenExceptionThrown() throws Exception {
        doThrow(new RuntimeException("ApplicationManagerTest.shouldStopApplicationWhenExceptionThrown()"))
            .when(mockJettyServer).stop();

        ReflectionTestUtils.setField(spyApplicationManager, "stage", mockStage);
        ReflectionTestUtils.setField(spyApplicationManager, "isInitialised", false);

        spyApplicationManager.stop();

        verify(mockMediaManager, times(1)).cleanUpResources();
        verify(mockSearchManager, times(1)).shutdown();
        verify(mockSettingsManager, never()).saveWindowSettings(mockStage);
        verify(mockSettingsManager, never()).saveUserSettings();
        verify(mockJettyServer, times(1)).stop();
    }

    @Test
    public void shouldShutdownApplication() {
        spyApplicationManager.shutdown();

        verify(mockApplicationContext, times(1)).close();
    }
}
