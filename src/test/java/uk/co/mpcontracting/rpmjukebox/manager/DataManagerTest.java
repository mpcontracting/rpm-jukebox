package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getTestResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SearchManager searchManager;

    @Mock
    private InternetManager internetManager;

    private DataManager underTest;

    @Before
    public void setup() {
        underTest = new DataManager(appProperties, new HashGenerator());
        underTest.wireSearchManager(searchManager);
        underTest.wireInternetManager(internetManager);
    }

    @Test
    @SneakyThrows
    public void shouldParseDataFile() {
        when(appProperties.getS3BucketUrl()).thenReturn("s3-bucket-url/");

        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());
        URLConnection urlConnection = mock(URLConnection.class);

        when(internetManager.openConnection(dataFile)).thenReturn(urlConnection);
        when(urlConnection.getInputStream()).thenReturn(dataFile.openStream());

        underTest.parse(dataFile);

        ArgumentCaptor<Artist> artistCaptor = ArgumentCaptor.forClass(Artist.class);
        ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);

        verify(searchManager, times(4)).addArtist(artistCaptor.capture());
        verify(searchManager, times(5)).addTrack(trackCaptor.capture());

        List<Artist> artists = artistCaptor.getAllValues();

        assertThat(artists)
                .allMatch(artist -> artist.getArtistId() != null)
                .allMatch(artist -> artist.getArtistName().startsWith("Test Artist ") && artist.getArtistName().length() == 13)
                .allMatch(artist -> artist.getArtistImage().equals("s3-bucket-url/image/artist/" + artist.getArtistId()))
                .allMatch(artist -> artist.getBiography().startsWith("Test Biography ") && artist.getBiography().length() == 16)
                .allMatch(artist -> artist.getMembers().startsWith("Test Members ") && artist.getMembers().length() == 14);

        List<Track> tracks = trackCaptor.getAllValues();

        assertThat(tracks)
                .allMatch(track -> track.getArtistId() != null)
                .allMatch(track -> track.getArtistName().startsWith("Test Artist ") && track.getArtistName().length() == 13)
                .allMatch(track -> track.getAlbumId() != null)
                .allMatch(track -> track.getAlbumName().startsWith("Test Album ") && track.getAlbumName().length() == 12)
                .allMatch(track -> track.getAlbumImage().equals("s3-bucket-url/image/album/" + track.getYear() + "/" +
                        track.getArtistId() + "/" + track.getAlbumId()))
                .allMatch(track -> track.getYear() == 2000)
                .allMatch(track -> track.getTrackId() != null && track.getTrackId().length() == 64)
                .allMatch(track -> track.getTrackName().startsWith("Test Track Name ") && track.getTrackName().length() == 17)
                .allMatch(track -> track.getIndex() > 0)
                .allMatch(track -> track.getLocation().equals("s3-bucket-url/music/" + track.getYear() + "/" + track.getArtistId() +
                        "/" + track.getAlbumId() + "/00" + track.getIndex()))
                .allMatch(track -> !track.isPreferred())
                .allMatch(track -> !track.getGenres().isEmpty());
    }

    @Test
    @SneakyThrows
    public void shouldNotParseDataFileOnException() {
        doThrow(new RuntimeException("DataManagerTest.shouldNotParseDataFileOnException()")).when(internetManager)
                .openConnection(any());

        URL dataFile = new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath());

        underTest.parse(dataFile);

        verify(searchManager, never()).addArtist(any());
        verify(searchManager, never()).addTrack(any());
    }
}
