package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackTableModelTest extends AbstractTest {

    @Test
    public void shouldInitialise() {
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789", 
            "Track Name", 1, "Location", true, Arrays.asList("Genre 1", "Genre 2"));
        TrackTableModel trackTableModel = new TrackTableModel(track);
        
        assertThat("Tracks should be equal", trackTableModel.getTrack(), equalTo(track));
        assertThat("Track ID should be '789'", trackTableModel.getTrackId().get(), equalTo("789"));
        assertThat("Track name should be 'Track Name'", trackTableModel.getTrackName().get(), equalTo("Track Name"));
        assertThat("Artist name should be 'Artist Name'", trackTableModel.getArtistName().get(), equalTo("Artist Name"));
        assertThat("Album year should be 2000", trackTableModel.getAlbumYear().get(), equalTo(2000));
        assertThat("Album name should be 'Album Name'", trackTableModel.getAlbumName().get(), equalTo("Album Name"));
        assertThat("Genres should be 'Genre 1, Genre 2'", trackTableModel.getGenres().get(), equalTo("Genre 1, Genre 2"));
    }
    
    @Test
    public void shouldInitialiseWithNoGenres() {
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789", 
            "Track Name", 1, "Location", true, null);
        TrackTableModel trackTableModel = new TrackTableModel(track);
        
        assertThat("Tracks should be equal", trackTableModel.getTrack(), equalTo(track));
        assertThat("Track ID should be '789'", trackTableModel.getTrackId().get(), equalTo("789"));
        assertThat("Track name should be 'Track Name'", trackTableModel.getTrackName().get(), equalTo("Track Name"));
        assertThat("Artist name should be 'Artist Name'", trackTableModel.getArtistName().get(), equalTo("Artist Name"));
        assertThat("Album year should be 2000", trackTableModel.getAlbumYear().get(), equalTo(2000));
        assertThat("Album name should be 'Album Name'", trackTableModel.getAlbumName().get(), equalTo("Album Name"));
        assertThat("Genres should be ''", trackTableModel.getGenres().get(), equalTo(""));
    }
}
