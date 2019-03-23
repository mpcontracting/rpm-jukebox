package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.component.LoveButtonTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableView;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@FXMLController
public class TrackTableController extends EventAwareObject {

    @FXML
    private TrackTableView<TrackTableModel> trackTableView;

    @FXML
    private TableColumn<TrackTableModel, String> loveColumn;

    @FXML
    private TableColumn<TrackTableModel, String> trackNameColumn;

    @FXML
    private TableColumn<TrackTableModel, String> artistNameColumn;

    @FXML
    private TableColumn<TrackTableModel, Number> albumYearColumn;

    @FXML
    private TableColumn<TrackTableModel, String> albumNameColumn;

    @FXML
    private TableColumn<TrackTableModel, String> genresColumn;

    private PlaylistManager playlistManager;

    private ObservableList<TrackTableModel> observableTracks;
    private int visiblePlaylistId;

    @Autowired
    private void wirePlaylistManager(PlaylistManager playlistManager) {
        this.playlistManager = playlistManager;
    }

    @FXML
    public void initialize() {
        log.info("Initialising TrackTableController");

        // Track table view
        observableTracks = FXCollections.observableArrayList();
        trackTableView.setPlaceholder(new Label(""));
        trackTableView.setItems(observableTracks);
        trackTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                if (trackTableView.getSelectionModel().getSelectedItem() != null) {
                    Track track = trackTableView.getSelectionModel().getSelectedItem().getTrack();

                    playlistManager.removeTrackFromPlaylist(track.getPlaylistId(), track);
                }
            }
        });

        // Cell factories
        loveColumn.setCellFactory(new LoveButtonTableCellFactory<>());
        trackNameColumn.setCellFactory(new TrackTableCellFactory<>());
        artistNameColumn.setCellFactory(new TrackTableCellFactory<>());
        albumYearColumn.setCellFactory(new TrackTableCellFactory<>());
        albumNameColumn.setCellFactory(new TrackTableCellFactory<>());
        genresColumn.setCellFactory(new TrackTableCellFactory<>());

        // Cell value factories
        loveColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackId());
        trackNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackName());
        artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().getArtistName());
        albumYearColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumYear());
        albumNameColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumName());
        genresColumn.setCellValueFactory(cellData -> cellData.getValue().getGenres());

        // State variables
        visiblePlaylistId = playlistManager.getCurrentPlaylistId();
    }

    private void updateObservableTracks(int playlistId) {
        log.debug("Updating observable tracks - {}", playlistId);

        observableTracks.clear();

        playlistManager.getPlaylist(playlistId).forEach(track -> observableTracks.add(new TrackTableModel(track)));
    }

    public Track getSelectedTrack() {
        TrackTableModel trackTableModel = trackTableView.getSelectionModel().getSelectedItem();

        if (trackTableModel != null) {
            return trackTableModel.getTrack();
        }

        return null;
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            case PLAYLIST_CONTENT_UPDATED: {
                if (payload != null && payload.length > 0) {
                    Integer playlistId = (Integer)payload[0];

                    if (playlistId.equals(visiblePlaylistId)) {
                        updateObservableTracks(playlistId);

                        if (payload.length > 1) {
                            trackTableView.highlightTrack((Track)payload[1]);
                        }
                    }
                }

                break;
            }
            case PLAYLIST_CREATED:
            case PLAYLIST_DELETED:
            case PLAYLIST_SELECTED: {
                if (payload != null && payload.length > 0) {
                    Integer playlistId = (Integer)payload[0];

                    if (!playlistId.equals(visiblePlaylistId)) {
                        visiblePlaylistId = playlistId;
                        updateObservableTracks(visiblePlaylistId);
                    }

                    trackTableView.highlightTrack(playlistManager.getTrackAtPlayingPlaylistIndex());
                }

                break;
            }
            case TRACK_QUEUED_FOR_PLAYING: {
                if (payload != null && payload.length > 0) {
                    Track track = (Track)payload[0];

                    // Set the track as selected in the table view
                    if (track.getPlaylistId() == visiblePlaylistId
                        && track.getPlaylistId() == playlistManager.getCurrentPlaylistId()) {
                        trackTableView.highlightTrack(track);
                    }
                }

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
