package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Objects.nonNull;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.component.LoveButtonTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableView;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class TrackTableController extends EventAwareObject {

  @FXML
  private TrackTableView trackTableView;

  @FXML
  private TableColumn<TrackTableModel, String> loveColumn;

  @FXML
  private TableColumn<TrackTableModel, String> trackNameColumn;

  @FXML
  private TableColumn<TrackTableModel, String> artistNameColumn;

  @FXML
  private TableColumn<TrackTableModel, String> albumNameColumn;

  @FXML
  private TableColumn<TrackTableModel, Number> albumYearColumn;

  @FXML
  private TableColumn<TrackTableModel, String> genresColumn;

  private final PlaylistService playlistService;
  private final SettingsService settingsService;
  private final StringResourceService stringResourceService;

  private ObservableList<TrackTableModel> observableTracks;
  private int visiblePlaylistId;

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

          log.info("Removing track - {}", track);

          playlistService.removeTrackFromPlaylist(track.getPlaylistId(), track);
        }
      }
    });

    // Cell factories
    loveColumn.setCellFactory(new LoveButtonTableCellFactory(playlistService));
    trackNameColumn.setCellFactory(new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService));
    artistNameColumn.setCellFactory(new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService));
    albumYearColumn.setCellFactory(new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService));
    albumNameColumn.setCellFactory(new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService));
    genresColumn.setCellFactory(new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService));

    // Cell value factories
    loveColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackId());
    trackNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackName());
    artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().getArtistName());
    albumYearColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumYear());
    albumNameColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumName());
    genresColumn.setCellValueFactory(cellData -> cellData.getValue().getGenres());

    // State variables
    visiblePlaylistId = playlistService.getCurrentPlaylistId();
  }

  private void updateObservableTracks(int playlistId) {
    log.debug("Updating observable tracks - {}", playlistId);

    observableTracks.clear();

    playlistService.getPlaylist(playlistId).ifPresent(playlist -> playlist.forEach(track -> observableTracks.add(new TrackTableModel(track))));
  }

  public Track getSelectedTrack() {
    TrackTableModel trackTableModel = trackTableView.getSelectionModel().getSelectedItem();

    if (nonNull(trackTableModel)) {
      return trackTableModel.getTrack();
    }

    return null;
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    switch (event) {
      case PLAYLIST_CONTENT_UPDATED -> {
        if (nonNull(payload) && payload.length > 0) {
          Integer playlistId = (Integer) payload[0];

          if (playlistId.equals(visiblePlaylistId)) {
            updateObservableTracks(playlistId);

            if (payload.length > 1) {
              trackTableView.highlightTrack((Track) payload[1]);
            }
          }
        }
      }
      case PLAYLIST_CREATED, PLAYLIST_DELETED, PLAYLIST_SELECTED -> {
        if (nonNull(payload) && payload.length > 0) {
          Integer playlistId = (Integer) payload[0];

          if (!playlistId.equals(visiblePlaylistId)) {
            visiblePlaylistId = playlistId;
            updateObservableTracks(visiblePlaylistId);
          }

          trackTableView.highlightTrack(playlistService.getTrackAtPlayingPlaylistIndex());
        }
      }
      case TRACK_QUEUED_FOR_PLAYING -> {
        if (nonNull(payload) && payload.length > 0) {
          Track track = (Track) payload[0];

          // Set the track as selected in the table view
          if (track.getPlaylistId() == visiblePlaylistId &&
              track.getPlaylistId() == playlistService.getCurrentPlaylistId()) {
            trackTableView.highlightTrack(track);
          }
        }
      }
    }
  }
}
