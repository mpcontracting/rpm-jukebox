package uk.co.mpcontracting.rpmjukebox.manager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import java.net.URL;

import static org.mockito.Mockito.*;

public class DataManagerTest extends AbstractTest {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private InternetManager internetManager;

    @Mock
    private SearchManager mockSearchManager;

    private DataManager spyDataManager;
    private InternetManager spyInternetManager;

    @Before
    public void setup() {
        spyInternetManager = spy(internetManager);
        spyDataManager = spy(new DataManager(appProperties, mockSearchManager, spyInternetManager));
        spyDataManager.setup();
    }

    @Test
    public void shouldParseDataFile() throws Exception {
        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());

        spyDataManager.parse(dataFile);

        verify(mockSearchManager, times(4)).addArtist(any());
        verify(mockSearchManager, times(5)).addTrack(any());
    }

    @Test
    public void shouldNotParseDataFileOnException() throws Exception {
        doThrow(new RuntimeException("DataManagerTest.shouldNotParseDataFileOnException()")).when(spyInternetManager)
            .openConnection(any());

        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());

        spyDataManager.parse(dataFile);

        verify(mockSearchManager, never()).addArtist(any());
        verify(mockSearchManager, never()).addTrack(any());
    }
}
