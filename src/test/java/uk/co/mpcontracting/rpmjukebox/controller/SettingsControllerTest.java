package uk.co.mpcontracting.rpmjukebox.controller;

import com.igormaznitsa.commons.version.Version;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

public class SettingsControllerTest extends AbstractGUITest implements Constants {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private SettingsController underTest;

    @Autowired
    private SettingsView originaSettingsView;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private SearchManager searchManager;

    @Mock
    private MainPanelController mainPanelController;

    private SettingsView settingsView;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(originaSettingsView);
    }

    @Before
    public void setup() {
        settingsView = spy(originaSettingsView);

        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "settingsManager", settingsManager);
        setField(underTest, "searchManager", searchManager);
        setField(underTest, "mainPanelController", mainPanelController);
        setField(underTest, "settingsView", settingsView);

        doNothing().when(settingsView).close();
    }

    @Test
    @SneakyThrows
    public void shouldBindSystemSettings() {
        SystemSettings systemSettings = mock(SystemSettings.class);
        when(systemSettings.getCacheSizeMb()).thenReturn(250);
        when(systemSettings.getProxyHost()).thenReturn("localhost");
        when(systemSettings.getProxyPort()).thenReturn(8080);
        when(systemSettings.getProxyUsername()).thenReturn("username");
        when(systemSettings.getProxyPassword()).thenReturn("password");
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);
        when(settingsManager.getVersion()).thenReturn(new Version("99.99.99"));

        threadRunner.runOnGui(() -> underTest.bindSystemSettings());

        WaitForAsyncUtils.waitForFxEvents();

        boolean valid = requireNonNull(invokeMethod(underTest, "validate"));

        assertThat(valid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldClickReindexButton() {
        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean) getField(underTest, "isReindexing");

        assertThat(isReindexing).isTrue();
        verify(mainPanelController, times(1)).showMessageView(anyString(), eq(false));
        verify(searchManager, times(1)).indexData();
        verify(mainPanelController, never()).closeMessageView();
    }

    @Test
    @SneakyThrows
    public void shouldClickReindexButtonAndThrowExceptionOnIndexData() {
        doThrow(new RuntimeException("SettingsControllerTest.shouldClickReindexButtonAndThrowExceptionOnIndexData()"))
                .when(searchManager).indexData();

        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean) getField(underTest, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mainPanelController, times(1)).showMessageView(anyString(), eq(false));
        verify(searchManager, times(1)).indexData();
        verify(mainPanelController, times(1)).closeMessageView();
    }

    @Test
    public void shouldClickOkButton() {
        TextField cacheSizeMbTextField = find("#cacheSizeMbTextField");
        cacheSizeMbTextField.setText("250");
        TextField proxyHostTextField = find("#proxyHostTextField");
        proxyHostTextField.setText("localhost");
        TextField proxyPortTextField = find("#proxyPortTextField");
        proxyPortTextField.setText("8080");
        CheckBox proxyAuthCheckBox = find("#proxyAuthCheckBox");
        proxyAuthCheckBox.setSelected(true);
        TextField proxyUsernameTextField = find("#proxyUsernameTextField");
        proxyUsernameTextField.setText("username");
        PasswordField proxyPasswordTextField = find("#proxyPasswordTextField");
        proxyPasswordTextField.setText("password");

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, times(1)).setCacheSizeMb(250);
        verify(systemSettings, times(1)).setProxyHost("localhost");
        verify(systemSettings, times(1)).setProxyPort(8080);
        verify(systemSettings, times(1)).setProxyRequiresAuthentication(true);
        verify(systemSettings, times(1)).setProxyUsername("username");
        verify(systemSettings, times(1)).setProxyPassword("password");
        verify(settingsManager, times(1)).saveSystemSettings();
        verify(settingsView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWhenCacheSizeIsInvalid() {
        TextField cacheSizeMbTextField = find("#cacheSizeMbTextField");
        cacheSizeMbTextField.setText("abc");

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, never()).setCacheSizeMb(anyInt());
        verify(systemSettings, never()).setProxyHost(anyString());
        verify(systemSettings, never()).setProxyPort(anyInt());
        verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(systemSettings, never()).setProxyUsername(anyString());
        verify(systemSettings, never()).setProxyPassword(anyString());
        verify(settingsManager, never()).saveSystemSettings();
        verify(settingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyHostIsInvalid() {
        TextField proxyHostTextField = find("#proxyHostTextField");
        proxyHostTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, never()).setCacheSizeMb(anyInt());
        verify(systemSettings, never()).setProxyHost(anyString());
        verify(systemSettings, never()).setProxyPort(anyInt());
        verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(systemSettings, never()).setProxyUsername(anyString());
        verify(systemSettings, never()).setProxyPassword(anyString());
        verify(settingsManager, never()).saveSystemSettings();
        verify(settingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyPortIsInvalid() {
        TextField proxyPortTextField = find("#proxyPortTextField");
        proxyPortTextField.setText("65536");

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, never()).setCacheSizeMb(anyInt());
        verify(systemSettings, never()).setProxyHost(anyString());
        verify(systemSettings, never()).setProxyPort(anyInt());
        verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(systemSettings, never()).setProxyUsername(anyString());
        verify(systemSettings, never()).setProxyPassword(anyString());
        verify(settingsManager, never()).saveSystemSettings();
        verify(settingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyUsernameIsInvalid() {
        TextField proxyUsernameTextField = find("#proxyUsernameTextField");
        proxyUsernameTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, never()).setCacheSizeMb(anyInt());
        verify(systemSettings, never()).setProxyHost(anyString());
        verify(systemSettings, never()).setProxyPort(anyInt());
        verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(systemSettings, never()).setProxyUsername(anyString());
        verify(systemSettings, never()).setProxyPassword(anyString());
        verify(settingsManager, never()).saveSystemSettings();
        verify(settingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyPasswordIsInvalid() {
        PasswordField proxyPasswordTextField = find("#proxyPasswordTextField");
        proxyPasswordTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings systemSettings = mock(SystemSettings.class);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        clickOn("#okButton");

        verify(systemSettings, never()).setCacheSizeMb(anyInt());
        verify(systemSettings, never()).setProxyHost(anyString());
        verify(systemSettings, never()).setProxyPort(anyInt());
        verify(systemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(systemSettings, never()).setProxyUsername(anyString());
        verify(systemSettings, never()).setProxyPassword(anyString());
        verify(settingsManager, never()).saveSystemSettings();
        verify(settingsView, never()).close();
    }

    @Test
    public void shouldClickCancelButton() {
        clickOn("#cancelButton");

        verify(settingsView, times(1)).close();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenIndexing() {
        setField(underTest, "isReindexing", true);

        underTest.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean) getField(underTest, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mainPanelController, times(1)).closeMessageView();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenNotIndexing() {
        setField(underTest, "isReindexing", false);

        underTest.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean) getField(underTest, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mainPanelController, never()).closeMessageView();
    }
}
