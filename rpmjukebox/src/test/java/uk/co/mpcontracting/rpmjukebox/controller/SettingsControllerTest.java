package uk.co.mpcontracting.rpmjukebox.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.igormaznitsa.commons.version.Version;

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

        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");

        assertThat("Cache size border should be '" + STYLE_VALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_VALID_BORDER));
    }

    @Test
    public void shouldNotBindSystemSettingsWhenCacheSizeTooSmall() throws Exception {
        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSystemSettings.getCacheSizeMb()).thenReturn(25);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
        when(mockSettingsManager.getVersion()).thenReturn(new Version("99.99.99"));

        CountDownLatch latch = new CountDownLatch(1);

        ThreadRunner.runOnGui(() -> {
            settingsController.bindSystemSettings();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");

        assertThat("Cache size border should be '" + STYLE_INVALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_INVALID_BORDER));
    }

    @Test
    public void shouldNotBindSystemSettingsWhenCacheSizeTooLarge() throws Exception {
        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSystemSettings.getCacheSizeMb()).thenReturn(2500);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
        when(mockSettingsManager.getVersion()).thenReturn(new Version("99.99.99"));

        CountDownLatch latch = new CountDownLatch(1);

        ThreadRunner.runOnGui(() -> {
            settingsController.bindSystemSettings();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");

        assertThat("Cache size border should be '" + STYLE_INVALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_INVALID_BORDER));
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
        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");
        cacheSizeMbTextField.setText("250");

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        assertThat("Cache size border should be '" + STYLE_VALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_VALID_BORDER));
        verify(mockSystemSettings, times(1)).setCacheSizeMb(250);
        verify(spySettingsView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWhenCacheSizeIsNull() {
        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");
        cacheSizeMbTextField.setText(null);

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        assertThat("Cache size border should be '" + STYLE_INVALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_INVALID_BORDER));
        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenCacheSizeIsEmpty() {
        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");
        cacheSizeMbTextField.setText("");

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        assertThat("Cache size border should be '" + STYLE_INVALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_INVALID_BORDER));
        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
        verify(spySettingsView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWhenCacheSizeIsAnInvalidInteger() {
        TextField cacheSizeMbTextField = (TextField)ReflectionTestUtils.getField(settingsController,
            "cacheSizeMbTextField");
        cacheSizeMbTextField.setText("abc");

        SystemSettings mockSystemSettings = mock(SystemSettings.class);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);

        clickOn("#okButton");

        assertThat("Cache size border should be '" + STYLE_INVALID_BORDER + "'", cacheSizeMbTextField.getStyle(),
            equalTo(STYLE_INVALID_BORDER));
        verify(mockSystemSettings, never()).setCacheSizeMb(anyInt());
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
