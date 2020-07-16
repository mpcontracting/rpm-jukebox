package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Before;
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
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getNonNullField;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistTest {

    private Playlist underTest;

    @Before
    public void setup() {
        underTest = createPlaylist(1, "Playlist", 10);
    }

    @Test
    public void shouldSetPlaylistId() {
        @SuppressWarnings("unchecked")
        List<Track> tracks = (List<Track>) getNonNullField(underTest, "tracks");

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getNonNullField(underTest, "shuffledTracks");

        Track track = spy(tracks.get(0));
        tracks.set(0, track);

        for (int i = 0; i < tracks.size(); i++) {
            if (shuffledTracks.get(i).equals(track)) {
                shuffledTracks.set(i, track);
                break;
            }
        }

        underTest.setPlaylistId(2);

        verify(track, times(2)).setPlaylistId(2);
    }

    @Test
    public void shouldGetTrackAtIndex() {
        Track track = underTest.getTrackAtIndex(5);

        assertThat(track.getTrackId()).isEqualTo("7896");
    }

    @Test
    public void shouldFailToGetTrackAtIndex() {
        Track track = underTest.getTrackAtIndex(10);

        assertThat(track).isNull();
    }

    @Test
    public void shouldGetPlaylistTrack() {
        Track track = generateTrack(1);

        Track result = underTest.getPlaylistTrack(track);

        assertThat(result).isEqualTo(track);
    }

    @Test
    public void shouldFailToGetPlaylistTrack() {
        Track track = generateTrack(0);

        Track result = underTest.getPlaylistTrack(track);

        assertThat(result).isNull();
    }

    @Test
    public void shouldGetShuffledTrackAtIndex() {
        Track track = underTest.getShuffledTrackAtIndex(5);

        assertThat(track).isNotNull();
    }

    @Test
    public void shouldFailToGetShuffledTrackAtIndex() {
        Track track = underTest.getShuffledTrackAtIndex(10);

        assertThat(track).isNull();
    }

    @Test
    public void shouldShufflePlaylist() {
        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = spy((List<Track>) requireNonNull(getField(underTest, "shuffledTracks")));
        setField(underTest, "shuffledTracks", shuffledTracks);

        underTest.shuffle();

        verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldSetTrackAtShuffledIndex() {
        Track track = underTest.getTrackAtIndex(1);

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = spy((List<Track>) requireNonNull(getField(underTest, "shuffledTracks")));
        setField(underTest, "shuffledTracks", shuffledTracks);

        underTest.setTrackAtShuffledIndex(track, 5);

        verify(shuffledTracks, times(1)).remove(track);
        verify(shuffledTracks, times(1)).add(5, track);
    }

    @Test
    public void shouldGetIsTrackInPlaylist() {
        boolean result = underTest.isTrackInPlaylist("7895");

        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsNull() {
        boolean result = underTest.isTrackInPlaylist(null);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldNotGetIsTrackInPlaylistWhenTrackIdIsUnknown() {
        boolean result = underTest.isTrackInPlaylist("20");

        assertThat(result).isFalse();
    }

    @Test
    public void shouldSetTracks() {
        underTest.setTracks(asList(mock(Track.class), mock(Track.class)));

        assertThat(underTest.getTracks()).hasSize(2);
    }

    @Test
    public void shouldAddTrack() {
        Track track1 = generateTrack(1);
        Track track2 = generateTrack(2);
        underTest.setTracks(asList(track1, track2));

        Track addedTrack = generateTrack(3);

        underTest.addTrack(addedTrack);

        assertThat(underTest.getTracks()).hasSize(3);
        assertThat(addedTrack.getPlaylistId()).isEqualTo(1);
        assertThat(addedTrack.getPlaylistIndex()).isEqualTo(2);
    }

    @Test
    public void shouldNotAddTrackWhenItAlreadyExists() {
        Track track1 = generateTrack(1);
        Track track2 = generateTrack(2);
        underTest.setTracks(asList(track1, track2));

        underTest.addTrack(generateTrack(2));

        assertThat(underTest.getTracks()).hasSize(2);
    }

    @Test
    public void shouldNotAddTrackWhenPlaylistSizeAtMaximum() {
        Track track1 = generateTrack(1);
        Track track2 = generateTrack(2);
        Playlist playlist = new Playlist(1, "Playlist", 2);
        playlist.setTracks(asList(track1, track2));

        playlist.addTrack(generateTrack(3));

        assertThat(playlist.getTracks()).hasSize(2);
    }

    @Test
    public void shouldRemoveTrack() {
        underTest.removeTrack(generateTrack(1));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(underTest, "shuffledTracks");

        assertThat(underTest.getTracks()).hasSize(9);
        assertThat(shuffledTracks).hasSize(9);
    }

    @Test
    public void shouldNotRemoveTrackWhenItDoesntExist() {
        underTest.removeTrack(generateTrack(0));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(underTest, "shuffledTracks");

        assertThat(underTest.getTracks()).hasSize(10);
        assertThat(shuffledTracks).hasSize(10);
    }

    @Test
    public void shouldSwapTracksSourceLessThanTarget() {
        Track source = underTest.getPlaylistTrack(generateTrack(2));
        Track target = underTest.getPlaylistTrack(generateTrack(8));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = spy((List<Track>) requireNonNull(getField(underTest, "shuffledTracks")));
        setField(underTest, "shuffledTracks", shuffledTracks);

        underTest.swapTracks(source, target);

        assertThat(source.getPlaylistIndex()).isEqualTo(7);
        assertThat(target.getPlaylistIndex()).isEqualTo(6);
        verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldSwapTracksTargetLessThanSource() {
        Track source = underTest.getPlaylistTrack(generateTrack(8));
        Track target = underTest.getPlaylistTrack(generateTrack(2));

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = spy((List<Track>) requireNonNull(getField(underTest, "shuffledTracks")));
        setField(underTest, "shuffledTracks", shuffledTracks);

        underTest.swapTracks(source, target);

        assertThat(source.getPlaylistIndex()).isEqualTo(1);
        assertThat(target.getPlaylistIndex()).isEqualTo(2);
        verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
    }

    @Test
    public void shouldGetIterator() {
        Iterator<Track> iterator = underTest.iterator();

        assertThat(iterator.hasNext()).isTrue();
    }

    @Test
    public void shouldGetSize() {
        assertThat(underTest.size()).isEqualTo(10);
    }

    @Test
    public void shouldGetIsEmpty() {
        assertThat(underTest.isEmpty()).isFalse();
    }

    @Test
    public void shouldClearPlaylist() {
        underTest.clear();

        @SuppressWarnings("unchecked")
        List<Track> shuffledTracks = (List<Track>) getField(underTest, "shuffledTracks");

        assertThat(underTest.getTracks()).isEmpty();
        assertThat(shuffledTracks).isEmpty();
    }

    @Test
    public void shouldClonePlaylist() {
        Playlist clone = underTest.clone();

        int playlistMaxSize = (Integer) requireNonNull(getField(underTest, "maxPlaylistSize"));
        int cloneMaxSize = (Integer) requireNonNull(getField(clone, "maxPlaylistSize"));
        SecureRandom playlistRandom = (SecureRandom) getField(underTest, "random");
        SecureRandom cloneRandom = (SecureRandom) getField(clone, "random");

        @SuppressWarnings("unchecked")
        List<Track> playlistShuffledTracks = (List<Track>) getNonNullField(underTest, "shuffledTracks");

        @SuppressWarnings("unchecked")
        List<Track> cloneShuffledTracks = (List<Track>) getNonNullField(clone, "shuffledTracks");

        assertThat(clone).isNotSameAs(underTest);
        assertThat(clone.getPlaylistId()).isEqualTo(underTest.getPlaylistId());
        assertThat(clone.getName()).isEqualTo(underTest.getName());
        assertThat(cloneMaxSize).isEqualTo(playlistMaxSize);
        assertThat(getAreTrackListsEqual(clone.getTracks(), underTest.getTracks())).isTrue();
        assertThat(getAreTrackListsEqual(cloneShuffledTracks, playlistShuffledTracks)).isTrue();
        assertThat(cloneRandom).isNotSameAs(playlistRandom);
    }

    private Playlist createPlaylist(int playlistId, String playlistName, int maxPlaylistSize) {
        Playlist playlist = new Playlist(playlistId, playlistName, maxPlaylistSize);
        List<Track> tracks = new ArrayList<>();

        for (int i = 1; i <= maxPlaylistSize; i++) {
            tracks.add(generateTrack(i));
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
