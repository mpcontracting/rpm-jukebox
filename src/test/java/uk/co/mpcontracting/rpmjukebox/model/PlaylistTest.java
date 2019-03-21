package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistTest {

    @Test
    public void shouldSetPlaylistId() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        @SuppressWarnings("unchecked")
        List<Track> tracks = (List<Track>) getField(playlist, "tracks");

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(playlist, "shuffledTracks");

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

        assertThat(track.getTrackId()).isEqualTo("7896");
    }

    @Test
    public void shouldFailToGetTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getTrackAtIndex(10);

        assertThat(track).isNull();
    }

    @Test
    public void shouldGetPlaylistTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);

        Track result = playlist.getPlaylistTrack(track);

        assertThat(result).isEqualTo(track);
    }

    @Test
    public void shouldFailToGetPlaylistTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = new Track("1230", null, null, "4560", null, null, 2000, "7890", null, 1, null, false, null);

        Track result = playlist.getPlaylistTrack(track);

        assertThat(result).isNull();
    }

    @Test
    public void shouldGetShuffledTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getShuffledTrackAtIndex(5);

        assertThat(track).isNotNull();
    }

    @Test
    public void shouldFailToGetShuffledTrackAtIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getShuffledTrackAtIndex(10);

        assertThat(track).isNull();
    }

    @Test
    public void shouldShufflePlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>) requireNonNull(getField(playlist, "shuffledTracks")));
        setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.shuffle();

        verify(spyShuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldSetTrackAtShuffledIndex() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track track = playlist.getTrackAtIndex(1);

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>) requireNonNull(getField(playlist, "shuffledTracks")));
        setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.setTrackAtShuffledIndex(track, 5);

        verify(spyShuffledTracks, times(1)).remove(track);
        verify(spyShuffledTracks, times(1)).add(5, track);
    }

    @Test
    public void shouldGetIsTrackInPlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist("7895");

        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsNull() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist(null);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsUnknown() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        boolean result = playlist.isTrackInPlaylist("20");

        assertThat(result).isFalse();
    }

    @Test
    public void shouldSetTracks() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        playlist.setTracks(asList(mock(Track.class), mock(Track.class)));

        assertThat(playlist.getTracks()).hasSize(2);
    }

    @Test
    public void shouldAddTrack() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setTracks(asList(track1, track2));

        Track addedTrack = new Track("1233", null, null, "4563", null, null, 2000, "7893", null, 1, null, false, null);

        playlist.addTrack(addedTrack);

        assertThat(playlist.getTracks()).hasSize(3);
        assertThat(addedTrack.getPlaylistId()).isEqualTo(1);
        assertThat(addedTrack.getPlaylistIndex()).isEqualTo(2);
    }

    @Test
    public void shouldNotAddTrackWhenItAlreadyExists() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setTracks(asList(track1, track2));

        playlist.addTrack(new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null));

        assertThat(playlist.getTracks()).hasSize(2);
    }

    @Test
    public void shouldNotAddTrackWhenPlaylistSizeAtMaximum() {
        Track track1 = new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null);
        Track track2 = new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null);
        Playlist playlist = new Playlist(1, "Playlist", 2);
        playlist.setTracks(asList(track1, track2));

        playlist.addTrack(new Track("1233", null, null, "4563", null, null, 2000, "7893", null, 1, null, false, null));

        assertThat(playlist.getTracks()).hasSize(2);
    }

    @Test
    public void shouldRemoveTrack() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        playlist.removeTrack(new Track("1231", null, null, "4561", null, null, 2000, "7891", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(playlist, "shuffledTracks");

        assertThat(playlist.getTracks()).hasSize(9);
        assertThat(shuffledTracks).hasSize(9);
    }

    @Test
    public void shouldNotRemoveTrackWhenItDoesntExist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        playlist
            .removeTrack(new Track("1230", null, null, "4560", null, null, 2000, "7890", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(playlist, "shuffledTracks");

        assertThat(playlist.getTracks()).hasSize(10);
        assertThat(shuffledTracks).hasSize(10);
    }

    @Test
    public void shouldSwapTracksSourceLessThanTarget() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Track source = playlist.getPlaylistTrack(
            new Track("1232", null, null, "4562", null, null, 2000, "7892", null, 1, null, false, null));
        Track target = playlist.getPlaylistTrack(
            new Track("1238", null, null, "4568", null, null, 2000, "7898", null, 1, null, false, null));

        @SuppressWarnings("unchecked")
        List<Track> spyShuffledTracks = spy((List<Track>) requireNonNull(getField(playlist, "shuffledTracks")));
        setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.swapTracks(source, target);

        assertThat(source.getPlaylistIndex()).isEqualTo(7);
        assertThat(target.getPlaylistIndex()).isEqualTo(6);
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
        List<Track> spyShuffledTracks = spy((List<Track>) requireNonNull(getField(playlist, "shuffledTracks")));
        setField(playlist, "shuffledTracks", spyShuffledTracks);

        playlist.swapTracks(source, target);

        assertThat(source.getPlaylistIndex()).isEqualTo(1);
        assertThat(target.getPlaylistIndex()).isEqualTo(2);
        verify(spyShuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldGetIterator() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Iterator<Track> iterator = playlist.iterator();

        assertThat(iterator.hasNext()).isTrue();
    }

    @Test
    public void shouldGetSize() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        assertThat(playlist.size()).isEqualTo(10);
    }

    @Test
    public void shouldGetIsEmpty() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);

        assertThat(playlist.isEmpty()).isFalse();
    }

    @Test
    public void shouldClearPlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        playlist.clear();

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(playlist, "shuffledTracks");

        assertThat(playlist.getTracks()).isEmpty();
        assertThat(shuffledTracks).isEmpty();
    }

    @Test
    public void shouldClonePlaylist() {
        Playlist playlist = createPlaylist(1, "Playlist", 10);
        Playlist clone = playlist.clone();

        int playlistMaxSize = (Integer) requireNonNull(getField(playlist, "maxPlaylistSize"));
        int cloneMaxSize = (Integer) requireNonNull(getField(clone, "maxPlaylistSize"));
        SecureRandom playlistRandom = (SecureRandom) getField(playlist, "random");
        SecureRandom cloneRandom = (SecureRandom) getField(clone, "random");

        @SuppressWarnings("unchecked")
        List<Track> playlistShuffledTracks = (List<Track>) getField(playlist, "shuffledTracks");

        @SuppressWarnings("unchecked")
        List<Track> cloneShuffledTracks = (List<Track>) getField(clone, "shuffledTracks");

        assertThat(clone).isNotSameAs(playlist);
        assertThat(clone.getPlaylistId()).isEqualTo(playlist.getPlaylistId());
        assertThat(clone.getName()).isEqualTo(playlist.getName());
        assertThat(cloneMaxSize).isEqualTo(playlistMaxSize);
        assertThat(getAreTrackListsEqual(clone.getTracks(), playlist.getTracks())).isTrue();
        assertThat(getAreTrackListsEqual(cloneShuffledTracks, playlistShuffledTracks)).isTrue();
        assertThat(cloneRandom).isNotSameAs(playlistRandom);
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
