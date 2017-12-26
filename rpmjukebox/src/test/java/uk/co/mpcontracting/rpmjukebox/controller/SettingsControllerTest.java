package uk.co.mpcontracting.rpmjukebox.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.igormaznitsa.commons.version.Version;

import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;

public class SettingsControllerTest extends AbstractTest implements Constants {

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

    @PostConstruct
    public void constructView() throws Exception {
        init(settingsView);
    }

    @Before
    public void setup() {
        spySettingsView = spy(settingsView);

        ReflectionTestUtils.setField(settingsController, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(settingsController, "settingsManager", mockSettingsManager);
        ReflectionTestUtils.setField(settingsController, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(settingsController, "mainPanelController", mockMainPanelController);
        ReflectionTestUtils.setField(settingsController, "settingsView", spySettingsView);

        doNothing().when(spySettingsView).close();
    }

    @Test
    public void shouldBindSystemSettings() throws Exception {
        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSystemSettings.getCacheSizeMb()).thenReturn(250);
        when(mockSystemSettings.getProxyHost()).thenReturn("localhost");
        when(mockSystemSettings.getProxyPort()).thenReturn(8080);
        when(mockSystemSettings.getProxyUsername()).thenReturn("username");
        when(mockSystemSettings.getProxyPassword()).thenReturn("password");
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
        when(mockSettingsManager.getVersion()).thenReturn(new Version("99.99.99"));

        CountDownLatch latch = new CountDownLatch(1);

        ThreadRunner.runOnGui(() -> {
            settingsController.bindSystemSettings();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        boolean valid = (Boolean)ReflectionTestUtils.invokeMethod(settingsController, "validate");

        assertThat("Settings should be valid", valid, equalTo(true));
    }

    @Test
    public void shouldClickReindexButton() throws Exception {
        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean)ReflectionTestUtils.getField(settingsController, "isReindexing");

        assertThat("Reindexing should be true", isReindexing, equalTo(true));
        verify(mockSearchManager, times(1)).indexData(false);
        verify(mockMainPanelController, never()).closeMessageView();
    }

    @Test
    public void shouldClickReindexButtonAndThrowExceptionOnIndexData() throws Exception {
        doThrow(new RuntimeException("SettingsControllerTest.shouldClickReindexButtonAndThrowExceptionOnIndexData()"))
            .when(mockSearchManager).indexData(anyBoolean());

        clickOn("#reindexButton");

        // Wait for reindex to kick off
        Thread.sleep(250);

        boolean isReindexing = (boolean)ReflectionTestUtils.getField(settingsController, "isReindexing");

        assertThat("Reindexing should be false", isReindexing, equalTo(false));
        verify(mockSearchManager, times(1)).indexData(false);
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
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickCancelButton() {
        clickOn("#cancelButton");

        verify(spySettingsView, times(1)).close();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenIndexing() {
        ReflectionTestUtils.setField(settingsController, "isReindexing", true);

        settingsController.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean)ReflectionTestUtils.getField(settingsController, "isReindexing");

        assertThat("Is reindexing should be false", isReindexing, equalTo(false));
        verify(mockMainPanelController, times(1)).closeMessageView();
    }

    @Test
    public void shouldRespondToDataIndexedEventWhenNotIndexing() {
        ReflectionTestUtils.setField(settingsController, "isReindexing", false);

        settingsController.eventReceived(Event.DATA_INDEXED);

        boolean isReindexing = (boolean)ReflectionTestUtils.getField(settingsController, "isReindexing");

        assertThat("Is reindexing should be false", isReindexing, equalTo(false));
        verify(mockMainPanelController, never()).closeMessageView();
    }
}
