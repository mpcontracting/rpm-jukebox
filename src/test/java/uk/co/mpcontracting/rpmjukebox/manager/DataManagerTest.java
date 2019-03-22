package uk.co.mpcontracting.rpmjukebox.manager;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getTestResourceFile;

import java.net.URL;
import java.net.URLConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private InternetManager mockInternetManager;

    private DataManager dataManager;

    @Before
    public void setup() {
      dataManager = new DataManager(mockAppProperties, new HashGenerator());
        dataManager.wireSearchManager(mockSearchManager);
        dataManager.wireInternetManager(mockInternetManager);

        dataManager.initialise();
    }

    @Test
    public void shouldParseDataFile() throws Exception {
        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());
        URLConnection mockUrlConnection = mock(URLConnection.class);

        when(mockInternetManager.openConnection(dataFile)).thenReturn(mockUrlConnection);
        when(mockUrlConnection.getInputStream()).thenReturn(dataFile.openStream());

        dataManager.parse(dataFile);

        verify(mockSearchManager, times(4)).addArtist(any());
        verify(mockSearchManager, times(5)).addTrack(any());
    }

    @Test
    public void shouldNotParseDataFileOnException() throws Exception {
        doThrow(new RuntimeException("DataManagerTest.shouldNotParseDataFileOnException()")).when(mockInternetManager)
            .openConnection(any());

        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());

        dataManager.parse(dataFile);

        verify(mockSearchManager, never()).addArtist(any());
        verify(mockSearchManager, never()).addTrack(any());
    }
}
