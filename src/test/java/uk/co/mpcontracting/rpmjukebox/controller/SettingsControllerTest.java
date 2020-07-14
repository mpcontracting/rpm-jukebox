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
    private SettingsController settingsController;

    @Autowired
    private SettingsView settingsView;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private MainPanelController mockMainPanelController;

    private SettingsView spySettingsView;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(settingsView);
    }

    @Before
    public void setup() {
        spySettingsView = spy(settingsView);

        setField(settingsController, "eventManager", getMockEventManager());
        setField(settingsController, "settingsManager", mockSettingsManager);
        setField(settingsController, "searchManager", mockSearchManager);
        setField(settingsController, "mainPanelController", mockMainPanelController);
        setField(settingsController, "settingsView", spySettingsView);

        doNothing().when(spySettingsView).close();
    }

    @Test
    @SneakyThrows
    public void shouldBindSystemSettings() {
        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSystemSettings.getCacheSizeMb()).thenReturn(250);
        when(mockSystemSettings.getProxyHost()).thenReturn("localhost");
        when(mockSystemSettings.getProxyPort()).thenReturn(8080);
        when(mockSystemSettings.getProxyUsername()).thenReturn("username");
        when(mockSystemSettings.getProxyPassword()).thenReturn("password");
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
        when(mockSettingsManager.getVersion()).thenReturn(new Version("99.99.99"));

        threadRunner.runOnGui(() -> settingsController.bindSystemSettings());

        WaitForAsyncUtils.waitForFxEvents();

        boolean valid = requireNonNull(invokeMethod(settingsController, "validate"));

        assertThat(valid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldClickReindexButton() {
        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean) getField(settingsController, "isReindexing");

        assertThat(isReindexing).isTrue();
        verify(mockMainPanelController, times(1)).showMessageView(anyString(), eq(false));
        verify(mockSearchManager, times(1)).indexData();
        verify(mockMainPanelController, never()).closeMessageView();
    }

    @Test
    @SneakyThrows
    public void shouldClickReindexButtonAndThrowExceptionOnIndexData() {
        doThrow(new RuntimeException("SettingsControllerTest.shouldClickReindexButtonAndThrowExceptionOnIndexData()"))
                .when(mockSearchManager).indexData();

        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean) getField(settingsController, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mockMainPanelController, times(1)).showMessageView(anyString(), eq(false));
        verify(mockSearchManager, times(1)).indexData();
        verify(mockMainPanelController, times(1)).closeMessageView();
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

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, times(1)).setCacheSizeMb(250);
        verify(mockSystemSettings, times(1)).setProxyHost("localhost");
        verify(mockSystemSettings, times(1)).setProxyPort(8080);
        verify(mockSystemSettings, times(1)).setProxyRequiresAuthentication(true);
        verify(mockSystemSettings, times(1)).setProxyUsername("username");
        verify(mockSystemSettings, times(1)).setProxyPassword("password");
        verify(mockSettingsManager, times(1)).saveSystemSettings();
        verify(spySettingsView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWhenCacheSizeIsInvalid() {
        TextField cacheSizeMbTextField = find("#cacheSizeMbTextField");
        cacheSizeMbTextField.setText("abc");

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(mockSystemSettings, never()).setProxyHost(anyString());
        verify(mockSystemSettings, never()).setProxyPort(anyInt());
        verify(mockSystemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(mockSystemSettings, never()).setProxyUsername(anyString());
        verify(mockSystemSettings, never()).setProxyPassword(anyString());
        verify(mockSettingsManager, never()).saveSystemSettings();
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyHostIsInvalid() {
        TextField proxyHostTextField = find("#proxyHostTextField");
        proxyHostTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(mockSystemSettings, never()).setProxyHost(anyString());
        verify(mockSystemSettings, never()).setProxyPort(anyInt());
        verify(mockSystemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(mockSystemSettings, never()).setProxyUsername(anyString());
        verify(mockSystemSettings, never()).setProxyPassword(anyString());
        verify(mockSettingsManager, never()).saveSystemSettings();
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyPortIsInvalid() {
        TextField proxyPortTextField = find("#proxyPortTextField");
        proxyPortTextField.setText("65536");

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(mockSystemSettings, never()).setProxyHost(anyString());
        verify(mockSystemSettings, never()).setProxyPort(anyInt());
        verify(mockSystemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(mockSystemSettings, never()).setProxyUsername(anyString());
        verify(mockSystemSettings, never()).setProxyPassword(anyString());
        verify(mockSettingsManager, never()).saveSystemSettings();
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyUsernameIsInvalid() {
        TextField proxyUsernameTextField = find("#proxyUsernameTextField");
        proxyUsernameTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(mockSystemSettings, never()).setProxyHost(anyString());
        verify(mockSystemSettings, never()).setProxyPort(anyInt());
        verify(mockSystemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(mockSystemSettings, never()).setProxyUsername(anyString());
        verify(mockSystemSettings, never()).setProxyPassword(anyString());
        verify(mockSettingsManager, never()).saveSystemSettings();
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenProxyPasswordIsInvalid() {
        PasswordField proxyPasswordTextField = find("#proxyPasswordTextField");
        proxyPasswordTextField.setText(StringUtils.repeat('x', 256));

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(mockSystemSettings, never()).setProxyHost(anyString());
        verify(mockSystemSettings, never()).setProxyPort(anyInt());
        verify(mockSystemSettings, never()).setProxyRequiresAuthentication(anyBoolean());
        verify(mockSystemSettings, never()).setProxyUsername(anyString());
        verify(mockSystemSettings, never()).setProxyPassword(anyString());
        verify(mockSettingsManager, never()).saveSystemSettings();
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickCancelButton() {
        clickOn("#cancelButton");

        verify(spySettingsView, times(1)).close();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenIndexing() {
        setField(settingsController, "isReindexing", true);

        settingsController.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean) getField(settingsController, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mockMainPanelController, times(1)).closeMessageView();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenNotIndexing() {
        setField(settingsController, "isReindexing", false);

        settingsController.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean) getField(settingsController, "isReindexing");

        assertThat(isReindexing).isFalse();
        verify(mockMainPanelController, never()).closeMessageView();
    }
}
