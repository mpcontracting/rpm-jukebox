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

    @Mock
    private SearchManager mockSearchManager;

    private DataManager spyDataManager;

    @Before
    public void setup() {
        spyDataManager = spy(dataManager);
        ReflectionTestUtils.setField(spyDataManager, "searchManager", mockSearchManager);
    }

    @Test
    public void shouldParseDataFile() throws Exception {
        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());

        spyDataManager.parse(dataFile);

        verify(mockSearchManager, times(4)).addArtist(any());
        verify(mockSearchManager, times(5)).addTrack(any());
    }
}
