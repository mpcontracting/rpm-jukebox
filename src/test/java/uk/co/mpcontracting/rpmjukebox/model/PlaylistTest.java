package uk.co.mpcontracting.rpmjukebox.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistTest extends AbstractTest {

    @Test
    public void shouldSetPlaylistId() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        @SuppressWarnings("unchecked")
        List<Track> tracks = (List<Track>)ReflectionTestUtils.getField(playlist, "tracks");

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks");

        Track track = spy(tracks.get(0));
        tracks.set(0, track);

        for (int i = 0; i < tracks.size(); i++) {
            if (shuffledTracks.get(i).equals(track)) {
                shuffledTracks.set(i, track);
                break;
            }
        }

        playlist.setPlaylistId(2);

        verify(track, times(2)).setPlaylistId(2);
    }

    @Test
    public void shouldGetTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getTrackAtIndex(5);

        assertThat("Track ID should be 5", track.getTrackId(), equalTo("7896"));
    }

    @Test
    public void shouldFailToGetTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getTrackAtIndex(10);

        assertThat("Track should be null", track, nullValue());
    }

    @Test
    public void shouldGetPlaylistTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);

        Track result = playlist.getPlaylistTrack(track);

        assertThat("Tracks should be equal", result, equalTo(track));
    }

    @Test
    public void shouldFailToGetPlaylistTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        Track result = playlist.getPlaylistTrack(
            new Track("1230", null, null, "4560", null, null, 2000, "7890", null, 1, null, false, null));

        assertThat("Track should be null", result, nullValue());
    }

    @Test
    public void shouldGetShuffledTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getShuffledTrackAtIndex(5);

        assertThat("Track should not be null", track, notNullValue());
    }

    @Test
    public void shouldFailToGetShuffledTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getShuffledTrackAtIndex(10);

        assertThat("Track should be null", track, nullValue());
    }

    @Test
    public void shouldShufflePlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks"));
        ReflectionTestUtils.setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.shuffle();

        verify(spyShuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldSetTrackAtShuffledIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getTrackAtIndex(1);

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks"));
        ReflectionTestUtils.setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.setTrackAtShuffledIndex(track, 5);

        verify(spyShuffledTracks, times(1)).remove(track);
        verify(spyShuffledTracks, times(1)).add(5, track);
    }

    @Test
    public void shouldGetIsTrackInPlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist("7895");

        assertThat("Result should be true", result, equalTo(true));
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsNull() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist(null);

        assertThat("Result should be false", result, equalTo(false));
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsUnknown() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist("20");

        assertThat("Result should be false", result, equalTo(false));
    }

    @Test
    public void shouldSetTracks() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        playlist.setTracks(Arrays.asList(mock(Track.class), mock(Track.class)));

        assertThat("Playlist size should be 2", playlist.getTracks(), hasSize(2));
    }

    @Test
    public void shouldAddTrack() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setTracks(Arrays.asList(track1, track2));

        Track addedTrack = new Track("1233", null, null, "4563", null, null, 2000, "7893", null, 1, null, false, null);

        playlist.addTrack(addedTrack);

        assertThat("Playlist size should be 3", playlist.getTracks(), hasSize(3));
        assertThat("Added track should have a playlist ID of 1", addedTrack.getPlaylistId(), equalTo(1));
        assertThat("Added track should have a playlist index of 2", addedTrack.getPlaylistIndex(), equalTo(2));
    }

    @Test
    public void shouldNotAddTrackWhenItAlreadyExists() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setTracks(Arrays.asList(track1, track2));

        playlist.addTrack(new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null));

        assertThat("Playlist size should be 2", playlist.getTracks(), hasSize(2));
    }

    @Test
    public void shouldNotAddTrackWhenPlaylistSizeAtMaximum() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 2);
        playlist.setTracks(Arrays.asList(track1, track2));

        playlist.addTrack(new Track("1233", null, null, "4563", null, null, 2000, "7893", null, 1, null, false, null));

        assertThat("Playlist size should be 2", playlist.getTracks(), hasSize(2));
    }

    @Test
    public void shouldRemoveTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        playlist
            .removeTrack(new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks");

        assertThat("Tracks size should be 9", playlist.getTracks(), hasSize(9));
        assertThat("Shuffled tracks size should be 9", shuffledTracks, hasSize(9));
    }

    @Test
    public void shouldNotRemoveTrackWhenItDoesntExist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        playlist
            .removeTrack(new Track("1230", null, null, "4560", null, null, 2000, "7890", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks");

        assertThat("Tracks size should be 10", playlist.getTracks(), hasSize(10));
        assertThat("Shuffled tracks size should be 10", shuffledTracks, hasSize(10));
    }

    @Test
    public void shouldSwapTracksSourceLessThanTarget() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track source = playlist.getPlaylistTrack(
            new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null));
        Track target = playlist.getPlaylistTrack(
            new Track("1238", null, null, "4568", null, null, 2000, "7898", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks"));
        ReflectionTestUtils.setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.swapTracks(source, target);

        assertThat("Source should have a playlist index of 7", source.getPlaylistIndex(), equalTo(7));
        assertThat("Target should have a playlist index of 6", target.getPlaylistIndex(), equalTo(6));
        verify(spyShuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldSwapTracksTargetLessThanSource() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track target = playlist.getPlaylistTrack(
            new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null));
        Track source = playlist.getPlaylistTrack(
            new Track("1238", null, null, "4568", null, null, 2000, "7898", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks"));
        ReflectionTestUtils.setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.swapTracks(source, target);

        assertThat("Source should have a playlist index of 1", source.getPlaylistIndex(), equalTo(1));
        assertThat("Target should have a playlist index of 2", target.getPlaylistIndex(), equalTo(2));
        verify(spyShuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldGetIterator() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Iterator<Track> iterator = playlist.iterator();

        assertThat("Iterator has at least one track", iterator.hasNext(), equalTo(true));
    }

    @Test
    public void shouldGetSize() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        assertThat("Playlist should have a size of 10", playlist.size(), equalTo(10));
    }

    @Test
    public void shouldGetIsEmpty() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        assertThat("Playlist should not be empty", playlist.isEmpty(), equalTo(false));
    }

    @Test
    public void shouldClearPlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        playlist.clear();

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks");

        assertThat("Tracks should be empty", playlist.getTracks().isEmpty(), equalTo(true));
        assertThat("Shuffled tracks should be empty", shuffledTracks.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldClonePlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Playlist clone = playlist.clone();

        int playlistMaxSize = (Integer)ReflectionTestUtils.getField(playlist, "maxPlaylistSize");
        int cloneMaxSize = (Integer)ReflectionTestUtils.getField(clone, "maxPlaylistSize");
        SecureRandom playlistRandom = (SecureRandom)ReflectionTestUtils.getField(playlist, "random");
        SecureRandom cloneRandom = (SecureRandom)ReflectionTestUtils.getField(clone, "random");

        @SuppressWarnings("unchecked")
        List<Track> playlistShuffledTracks = (List<Track>)ReflectionTestUtils.getField(playlist, "shuffledTracks");

        @SuppressWarnings("unchecked")
        List<Track> cloneShuffledTracks = (List<Track>)ReflectionTestUtils.getField(clone, "shuffledTracks");

        assertThat("Clone should not be the same object as playlist", clone == playlist, equalTo(false));
        assertThat("Playlist ID should be equal", clone.getPlaylistId(), equalTo(playlist.getPlaylistId()));
        assertThat("Playlist name should be equal", clone.getName(), equalTo(playlist.getName()));
        assertThat("Playlist max play size should be equal", cloneMaxSize, equalTo(playlistMaxSize));
        assertThat("Playlist tracks should be equal", getAreTrackListsEqual(clone.getTracks(), playlist.getTracks()),
            equalTo(true));
        assertThat("Playlist shuffled tracks should be equal",
            getAreTrackListsEqual(cloneShuffledTracks, playlistShuffledTracks), equalTo(true));
        assertThat("Random generators should be different objects", cloneRandom == playlistRandom, equalTo(false));
    }

    private Playlist createPlaylist(int playlistId, String playlistName, int maxPlaylistSize) {
        Playlist playlist = new Playlist(playlistId, playlistName, maxPlaylistSize);
        List<Track> tracks = new ArrayList<>();

        for (int i = 1; i <= maxPlaylistSize; i++) {
            tracks.add(new Track("123" + i, null, null, "456" + i, null, null, 2000 + i, "789" + i, null, i, null,
                false, null));
        }

        playlist.setTracks(tracks);

        return playlist;
    }

    private boolean getAreTrackListsEqual(List<Track> tracks1, List<Track> tracks2) {
        if (tracks1.size() != tracks2.size()) {
            return false;
        }

        for (int i = 0; i < tracks1.size(); i++) {
            if (!tracks1.get(i).equals(tracks2.get(i))) {
                return false;
            }
        }

        return true;
    }
}
