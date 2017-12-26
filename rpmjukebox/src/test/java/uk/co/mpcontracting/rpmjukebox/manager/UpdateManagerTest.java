package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import com.igormaznitsa.commons.version.Version;

import de.felixroske.jfxsupport.GUIState;
import javafx.application.HostServices;
import okhttp3.Response;
import okhttp3.ResponseBody;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class UpdateManagerTest extends AbstractTest {

    @Autowired
    private UpdateManager updateManager;

    @Value("${website.url}")
    private String websiteUrl;

    @Mock
    private InternetManager mockInternetManager;

    @Mock
    private URL mockVersionUrl;

    @Mock
    private HostServices mockHostServices;

    private UpdateManager spyUpdateManager;

    @Before
    public void setup() throws Exception {
        spyUpdateManager = spy(updateManager);

        ReflectionTestUtils.setField(spyUpdateManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spyUpdateManager, "internetManager", mockInternetManager);
        ReflectionTestUtils.setField(spyUpdateManager, "versionUrl", mockVersionUrl);
        ReflectionTestUtils.setField(spyUpdateManager, "newVersion", null);
    }

    @Test
    public void shouldFindUpdatesAvailable() throws Exception {
        ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.byteStream()).thenReturn(new ByteArrayInputStream("99.99.99".getBytes()));

        Response mockResponse = mock(Response.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);

        when(mockInternetManager.openConnection(any())).thenReturn(mockResponse);

        spyUpdateManager.checkForUpdates();

        assertThat("New version should not be null", ReflectionTestUtils.getField(spyUpdateManager, "newVersion"),
            notNullValue());
        verify(getMockEventManager(), times(1)).fireEvent(Event.NEW_VERSION_AVAILABLE, new Version("99.99.99"));
    }

    @Test
    public void shouldNotFindUpdatesAvailable() throws Exception {
        ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.byteStream()).thenReturn(new ByteArrayInputStream("0.0.1".getBytes()));

        Response mockResponse = mock(Response.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);

        when(mockInternetManager.openConnection(any())).thenReturn(mockResponse);

        spyUpdateManager.checkForUpdates();

        assertThat("New version should be null", ReflectionTestUtils.getField(spyUpdateManager, "newVersion"),
            nullValue());
        verify(getMockEventManager(), never()).fireEvent(Event.NEW_VERSION_AVAILABLE, new Version("0.0.1"));
    }

    @Test
    public void shouldNotFindUpdatesOnConnectionError() throws Exception {
        doThrow(new RuntimeException("UpdateManagerTest.shouldNotFindUpdatesOnConnectionError()")).when(mockVersionUrl)
            .openConnection();

        spyUpdateManager.checkForUpdates();

        assertThat("New version should be null", ReflectionTestUtils.getField(spyUpdateManager, "newVersion"),
            nullValue());
        verify(getMockEventManager(), never()).fireEvent(Event.NEW_VERSION_AVAILABLE, (Version)null);
    }

    @Test
    public void shouldNotFindUpdatesOn404() throws Exception {
        Response mockResponse = mock(Response.class);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(404);

        when(mockInternetManager.openConnection(any())).thenReturn(mockResponse);

        spyUpdateManager.checkForUpdates();

        assertThat("New version should be null", ReflectionTestUtils.getField(spyUpdateManager, "newVersion"),
            nullValue());
        verify(getMockEventManager(), never()).fireEvent(Event.NEW_VERSION_AVAILABLE, (Version)null);
    }

    @Test
    public void shouldNotFindUpdatesAvailableOnEmptyVersionString() throws Exception {
        ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.byteStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));

        Response mockResponse = mock(Response.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);

        when(mockInternetManager.openConnection(any())).thenReturn(mockResponse);

        spyUpdateManager.checkForUpdates();

        assertThat("New version should be null", ReflectionTestUtils.getField(spyUpdateManager, "newVersion"),
            nullValue());
        verify(getMockEventManager(), never()).fireEvent(Event.NEW_VERSION_AVAILABLE, (Version)null);
    }

    @Test
    public void shouldDownloadNewVersion() throws Exception {
        HostServices existingHostServices = GUIState.getHostServices();
        ReflectionTestUtils.setField(GUIState.class, "hostServices", mockHostServices);

        spyUpdateManager.downloadNewVersion();

        // Wait for invocation
        Thread.sleep(500);

        ReflectionTestUtils.setField(GUIState.class, "hostServices", existingHostServices);

        verify(mockHostServices, times(1)).showDocument(websiteUrl);
    }

    @Test
    public void shouldCheckForUpdatesOnApplicationInitialisedEvent() throws Exception {
        doNothing().when(spyUpdateManager).checkForUpdates();

        spyUpdateManager.eventReceived(Event.APPLICATION_INITIALISED);

        // Wait for invocation
        Thread.sleep(500);

        verify(spyUpdateManager, times(1)).checkForUpdates();
    }
}
