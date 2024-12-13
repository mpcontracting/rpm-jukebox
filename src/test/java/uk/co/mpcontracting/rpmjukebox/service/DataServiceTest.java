package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getTestResourceFile;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.util.HashGenerator;

@ExtendWith(MockitoExtension.class)
class DataServiceTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private InternetService internetService;

  @Mock
  private SearchService searchService;

  private DataService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new DataService(applicationProperties, new HashGenerator(), internetService, searchService);
  }

  @Test
  @SneakyThrows
  void shouldParseDataFile() {
    when(applicationProperties.getS3BucketUrl()).thenReturn("s3-bucket-url/");

    URL dataFile = URI.create("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()).toURL();
    URLConnection urlConnection = mock(URLConnection.class);

    when(internetService.openConnection(dataFile)).thenReturn(urlConnection);
    when(urlConnection.getInputStream()).thenReturn(dataFile.openStream());

    underTest.parse(dataFile);

    ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);

    verify(searchService, times(5)).addTrack(trackCaptor.capture());

    List<Track> tracks = trackCaptor.getAllValues();

    assertThat(tracks)
        .allMatch(track -> nonNull(track.getArtistId()))
        .allMatch(track -> track.getArtistName().startsWith("Test Artist ") && track.getArtistName().length() == 13)
        .allMatch(track -> nonNull(track.getAlbumId()))
        .allMatch(track -> track.getAlbumName().startsWith("Test Album ") && track.getAlbumName().length() == 12)
        .allMatch(track -> track.getAlbumImage().equals("s3-bucket-url/image/album/" + track.getYear() + "/" +
            track.getArtistId() + "/" + track.getAlbumId()))
        .allMatch(track -> track.getYear() == 2000)
        .allMatch(track -> nonNull(track.getTrackId()) && track.getTrackId().length() == 64)
        .allMatch(track -> track.getTrackName().startsWith("Test Track Name ") && track.getTrackName().length() == 17)
        .allMatch(track -> track.getIndex() > 0)
        .allMatch(track -> track.getLocation().equals("s3-bucket-url/music/" + track.getYear() + "/" + track.getArtistId() +
            "/" + track.getAlbumId() + "/00" + track.getIndex()))
        .allMatch(track -> !track.isPreferred())
        .allMatch(track -> !track.getGenres().isEmpty());
  }

  @Test
  @SneakyThrows
  void shouldNotParseDataFileOnException() {
    doThrow(new RuntimeException("DataManagerTest.shouldNotParseDataFileOnException()")).when(internetService)
        .openConnection(any());

    URL dataFile = URI.create("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()).toURL();

    underTest.parse(dataFile);

    verify(searchService, never()).addTrack(any());
  }
}