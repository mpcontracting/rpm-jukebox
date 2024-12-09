package uk.co.mpcontracting.rpmjukebox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_WINDOW_TITLE;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.LINUX;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.OSX;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.WINDOWS;

import java.util.concurrent.Executors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.EventProcessor;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.test.util.TestThreadRunner;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;
import uk.co.mpcontracting.rpmjukebox.util.OsType;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@ExtendWith(MockitoExtension.class)
class ApplicationLifecycleServiceTest {

  @Mock
  private Environment environment;

  @Mock
  private RpmJukebox rpmJukebox;

  @Mock
  private JettyServer jettyServer;

  @Mock
  private MediaService mediaService;

  @Mock
  private SearchService searchService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private StringResourceService stringResourceService;

  @Mock
  private EventProcessor eventProcessor;

  @Mock
  private Stage stage;

  @Mock
  private ObservableList<Image> observableList;

  private ApplicationContext originalContext;
  private ApplicationLifecycleService underTest;

  @BeforeEach
  void beforeEach() {
    originalContext = getField(ContextHelper.class, "applicationContext", ApplicationContext.class);
    ApplicationContext mockContext = mock(ApplicationContext.class);
    setField(ContextHelper.class, "applicationContext", mockContext);

    lenient().when(mockContext.getBean(EventProcessor.class)).thenReturn(eventProcessor);
    lenient().when(stage.getIcons()).thenReturn(observableList);

    ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

    underTest = spy(new ApplicationLifecycleService(environment, threadRunner, rpmJukebox, jettyServer, mediaService, searchService, settingsService, stringResourceService));
    underTest.setApplicationContext(mockContext);
  }

  @AfterEach
  void afterEach() {
    setField(ContextHelper.class, "applicationContext", originalContext);
  }

  @Test
  @SneakyThrows
  void shouldInitialise() {
    when(environment.getActiveProfiles()).thenReturn(new String[]{"default"});

    underTest.initialise();

    verify(searchService).initialise();
    verify(settingsService).loadUserSettings();
  }

  @Test
  @SneakyThrows
  void shouldNotInitialiseForTestProfile() {
    when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

    underTest.initialise();

    verify(searchService, never()).initialise();
    verify(settingsService, never()).loadUserSettings();
  }

  @ParameterizedTest
  @MethodSource("getStartData")
  void shouldStart(OsType osType, VerificationMode verificationMode) {
    String windowTitle = getFaker().lorem().characters(10, 20);

    when(settingsService.getOsType()).thenReturn(osType);
    when(stringResourceService.getString(MESSAGE_WINDOW_TITLE)).thenReturn(windowTitle);
    setField(underTest, "stage", null);
    setField(underTest, "isInitialised", false);

    underTest.start(stage);

    assertThat(getField(underTest, "stage", Stage.class)).isNotNull();
    assertThat(getField(underTest, "isInitialised", Boolean.class)).isTrue();

    verify(stage).setTitle(windowTitle);
    verify(stage, verificationMode).getIcons();
    verify(settingsService).loadWindowSettings(stage);
    verify(eventProcessor).fireEvent(APPLICATION_INITIALISED);
  }

  private static Stream<Arguments> getStartData() {
    return Stream.of(
        Arguments.of(OSX, never()),
        Arguments.of(WINDOWS, times(1)),
        Arguments.of(LINUX, never())
    );
  }

  @Test
  @SneakyThrows
  void shouldStopApplication() {
    setField(underTest, "stage", stage);
    setField(underTest, "isInitialised", true);

    underTest.stop();

    verify(mediaService).cleanUpResources();
    verify(searchService).shutdown();
    verify(settingsService).saveWindowSettings(stage);
    verify(settingsService).saveUserSettings();
    verify(settingsService).saveSystemSettings();
    verify(jettyServer).stop();
  }

  @Test
  @SneakyThrows
  void shouldStopApplicationWhenNotInitialised() {
    setField(underTest, "stage", stage);
    setField(underTest, "isInitialised", false);

    underTest.stop();

    verify(mediaService).cleanUpResources();
    verify(searchService).shutdown();
    verify(settingsService, never()).saveWindowSettings(stage);
    verify(settingsService, never()).saveUserSettings();
    verify(settingsService, never()).saveSystemSettings();
    verify(jettyServer).stop();
  }

  @Test
  @SneakyThrows
  public void shouldStopApplicationWhenExceptionThrown() {
    doThrow(new RuntimeException("ApplicationManagerTest.shouldStopApplicationWhenExceptionThrown()"))
        .when(jettyServer).stop();

    setField(underTest, "stage", stage);
    setField(underTest, "isInitialised", false);

    underTest.stop();

    verify(mediaService).cleanUpResources();
    verify(searchService).shutdown();
    verify(settingsService, never()).saveWindowSettings(stage);
    verify(settingsService, never()).saveUserSettings();
    verify(settingsService, never()).saveSystemSettings();
    verify(jettyServer).stop();
  }
}