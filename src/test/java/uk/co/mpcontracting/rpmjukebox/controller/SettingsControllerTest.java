package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static uk.co.mpcontracting.rpmjukebox.event.Event.DATA_INDEXED;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createVersion;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import jakarta.annotation.PostConstruct;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.service.SearchService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;

class SettingsControllerTest extends AbstractGuiTest {

  @MockBean
  private MainPanelController mainPanelController;

  @MockBean
  private SearchService searchService;

  @MockBean
  private SettingsService settingsService;

  @SpyBean
  private SettingsView settingsView;

  @Autowired
  private SettingsController underTest;

  @SneakyThrows
  @PostConstruct
  void postConstruct() {
    init(settingsView);
  }

  @BeforeEach
  void beforeEach() {
    ((TextField) find("#cacheSizeMbTextField")).setText("");
    ((TextField) find("#proxyHostTextField")).setText("");
    ((TextField) find("#proxyPortTextField")).setText("");
    ((CheckBox) find("#proxyAuthCheckBox")).setSelected(false);
    ((TextField) find("#proxyUsernameTextField")).setText("");
    ((PasswordField) find("#proxyPasswordTextField")).setText("");

    doNothing().when(settingsView).close();
  }

  @Test
  void shouldBindSystemSettings() {
    SystemSettings systemSettings = mock(SystemSettings.class);
    when(systemSettings.getCacheSizeMb()).thenReturn(250);
    when(systemSettings.getProxyHost()).thenReturn("localhost");
    when(systemSettings.getProxyPort()).thenReturn(8080);
    when(systemSettings.getProxyUsername()).thenReturn("username");
    when(systemSettings.getProxyPassword()).thenReturn("password");
    when(settingsService.getSystemSettings()).thenReturn(systemSettings);
    when(settingsService.getVersion()).thenReturn(createVersion());

    Platform.runLater(() -> underTest.bindSystemSettings());

    WaitForAsyncUtils.waitForFxEvents();

    boolean valid = requireNonNull(invokeMethod(underTest, "validate"));

    assertThat(valid).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldClickReindexButton() {
    clickOn("#reindexButton");

    // Wait for reindex to kick off
    Thread.sleep(250);

    boolean isReindexing = getField(underTest, "isReindexing", Boolean.class);

    assertThat(isReindexing).isTrue();
    verify(mainPanelController).showMessageView(anyString(), eq(false));
    verify(searchService).indexData();
  }

  @Test
  @SneakyThrows
  void shouldClickReindexButtonAndThrowExceptionOnIndexData() {
    doThrow(new RuntimeException("SettingsControllerTest.shouldClickReindexButtonAndThrowExceptionOnIndexData()"))
        .when(searchService).indexData();

    clickOn("#reindexButton");

    // Wait for reindex to kick off
    Thread.sleep(250);

    boolean isReindexing = getField(underTest, "isReindexing", Boolean.class);

    assertThat(isReindexing).isFalse();
    verify(mainPanelController).showMessageView(anyString(), eq(false));
    verify(searchService).indexData();
    verify(mainPanelController).closeMessageView();
  }

  @Test
  void shouldClickOkButton() {
    int cacheSizeMb = getFaker().number().numberBetween(50, 1000);
    String proxyHost = getFaker().lorem().characters(10);
    int proxyPort = getFaker().number().numberBetween(80, 65535);
    boolean proxyAuth = getFaker().bool().bool();
    String username = getFaker().lorem().characters(10, 20);
    String password = getFaker().lorem().characters(10, 20);

    TextField cacheSizeMbTextField = find("#cacheSizeMbTextField");
    cacheSizeMbTextField.setText(Integer.toString(cacheSizeMb));
    TextField proxyHostTextField = find("#proxyHostTextField");
    proxyHostTextField.setText(proxyHost);
    TextField proxyPortTextField = find("#proxyPortTextField");
    proxyPortTextField.setText(Integer.toString(proxyPort));
    CheckBox proxyAuthCheckBox = find("#proxyAuthCheckBox");
    proxyAuthCheckBox.setSelected(proxyAuth);
    TextField proxyUsernameTextField = find("#proxyUsernameTextField");
    proxyUsernameTextField.setText(username);
    PasswordField proxyPasswordTextField = find("#proxyPasswordTextField");
    proxyPasswordTextField.setText(password);

    SystemSettings systemSettings = mock(SystemSettings.class);
    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    clickOn("#okButton");

    verify(systemSettings).setCacheSizeMb(cacheSizeMb);
    verify(systemSettings).setProxyHost(proxyHost);
    verify(systemSettings).setProxyPort(proxyPort);
    verify(systemSettings).setProxyRequiresAuthentication(proxyAuth);
    verify(systemSettings).setProxyUsername(username);
    verify(systemSettings).setProxyPassword(password);
    verify(settingsService).saveSystemSettings();
    verify(settingsView).close();
  }

  @ParameterizedTest
  @MethodSource("getFieldNodesAndValues")
  void shouldClickOkButtonWhenValueIsInvalid(Class<?> clazz, String query, String value) {
    Control control = find(query);

    if (clazz == TextField.class) {
      ((TextField) control).setText(value);
    } else if (clazz == PasswordField.class) {
      ((PasswordField) control).setText(value);
    }

    SystemSettings systemSettings = mock(SystemSettings.class);
    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    clickOn("#okButton");

    verify(systemSettings, never()).setCacheSizeMb(anyInt());
    verify(systemSettings, never()).setProxyHost(anyString());
    verify(systemSettings, never()).setProxyPort(anyInt());
    verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
    verify(systemSettings, never()).setProxyUsername(anyString());
    verify(systemSettings, never()).setProxyPassword(anyString());
    verify(settingsService, never()).saveSystemSettings();
    verify(settingsView, never()).close();
  }

  private static Stream<Arguments> getFieldNodesAndValues() {
    return Stream.of(
        Arguments.of(TextField.class, "#cacheSizeMbTextField", "abc"),
        Arguments.of(TextField.class, "#proxyHostTextField", repeat('x', 256)),
        Arguments.of(TextField.class, "#proxyPortTextField", "65536"),
        Arguments.of(TextField.class, "#proxyUsernameTextField", repeat('x', 256)),
        Arguments.of(PasswordField.class, "#proxyPasswordTextField", repeat('x', 256))
    );
  }

  @Test
  void shouldClickCancelButton() {
    clickOn("#cancelButton");

    verify(settingsView).close();
  }

  @Test
  void shouldRespondToDataIndexedEventWhenIndexing() {
    setField(underTest, "isReindexing", true);

    underTest.eventReceived(DATA_INDEXED);

    boolean isReindexing = getField(underTest, "isReindexing", Boolean.class);

    assertThat(isReindexing).isFalse();
    verify(mainPanelController).closeMessageView();
  }

  @Test
  void shouldRespondToDataIndexedEventWhenNotIndexing() {
    setField(underTest, "isReindexing", false);

    underTest.eventReceived(DATA_INDEXED);

    boolean isReindexing = getField(underTest, "isReindexing", Boolean.class);

    assertThat(isReindexing).isFalse();
    verify(mainPanelController, never()).closeMessageView();
  }
}