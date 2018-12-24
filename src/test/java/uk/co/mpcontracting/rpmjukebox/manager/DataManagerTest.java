package uk.co.mpcontracting.rpmjukebox.manager;

import static org.mockito.Mockito.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class DataManagerTest extends AbstractTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private InternetManager internetManager;

    @Mock
    private SearchManager mockSearchManager;

    private DataManager spyDataManager;
    private InternetManager spyInternetManager;

    @Before
    public void setup() {
        spyDataManager = spy(dataManager);
        spyInternetManager = spy(internetManager);
        ReflectionTestUtils.setField(spyDataManager, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(spyDataManager, "internetManager", spyInternetManager);
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
