package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.DND_TRACK_DATA_FORMAT;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_DRAG_N_DROP;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_TRACK_TABLE_CONTEXT_CREATE_PLAYLIST_FROM_ALBUM;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_TRACK_TABLE_CONTEXT_DELETE_TRACK_FROM_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.util.OsType;

@RequiredArgsConstructor
public class TrackTableCellFactory<T> extends EventAwareObject implements Callback<TableColumn<TrackTableModel, T>, TableCell<TrackTableModel, T>> {

  private final StringResourceService stringResourceService;
  private final SettingsService settingsService;
  private final PlaylistService playlistService;
  private final Image dragNDrop = new Image(IMAGE_DRAG_N_DROP);

  @Override
  public TableCell<TrackTableModel, T> call(TableColumn<TrackTableModel, T> tableColumn) {
    final TrackTableCell<T> tableCell = new TrackTableCell<>();

    //////////////////
    // Mouse Events //
    //////////////////

    tableCell.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY) {
        if (event.getClickCount() > 1) {
          // Double click
          ofNullable(tableCell.getItem()).ifPresent(item -> playlistService.playTrack(tableCell.getTableRow().getItem().getTrack()));
        } else {
          // Single click
          ofNullable(tableCell.getItem()).ifPresent(item -> fireEvent(TRACK_SELECTED, tableCell.getTableRow().getItem().getTrack()));
        }
      }
    });

    //////////////////
    // Context Menu //
    //////////////////

    ContextMenu contextMenu = new ContextMenu();

    final MenuItem createPlaylistFromAlbumItem = new MenuItem(stringResourceService.getString(MESSAGE_TRACK_TABLE_CONTEXT_CREATE_PLAYLIST_FROM_ALBUM));
    createPlaylistFromAlbumItem.setOnAction(event -> ofNullable(tableCell.getItem())
        .ifPresent(item -> playlistService.createPlaylistFromAlbum(tableCell.getTableRow().getItem().getTrack())));

    contextMenu.getItems().add(createPlaylistFromAlbumItem);

    final MenuItem deleteTrackFromPlaylistItem = new MenuItem(stringResourceService.getString(MESSAGE_TRACK_TABLE_CONTEXT_DELETE_TRACK_FROM_PLAYLIST));
    deleteTrackFromPlaylistItem.setOnAction(event ->
        ofNullable(tableCell.getItem()).ifPresent(item -> {
          Track track = tableCell.getTableRow().getItem().getTrack();

          playlistService.removeTrackFromPlaylist(track.getPlaylistId(), track);
        }));
    contextMenu.getItems().add(deleteTrackFromPlaylistItem);

    tableCell.setContextMenu(contextMenu);
    tableCell.setOnContextMenuRequested(event -> {
      if (nonNull(tableCell.getItem())) {
        if (tableCell.getTableRow().getItem().getTrack().getPlaylistId() == PLAYLIST_ID_SEARCH) {
          createPlaylistFromAlbumItem.setDisable(false);
          deleteTrackFromPlaylistItem.setDisable(true);
        } else {
          createPlaylistFromAlbumItem.setDisable(true);
          deleteTrackFromPlaylistItem.setDisable(false);
        }
      } else {
        createPlaylistFromAlbumItem.setDisable(true);
        deleteTrackFromPlaylistItem.setDisable(true);
      }
    });

    ///////////////////
    // Drag And Drop //
    ///////////////////

    tableCell.setOnDragDetected(event -> {
      if (nonNull(tableCell.getItem())) {
        Track track = tableCell.getTableRow().getItem().getTrack();
        Dragboard dragboard = tableCell.startDragAndDrop(TransferMode.COPY_OR_MOVE);

        // Only set the drag and drop image on OSX
        if (settingsService.getOsType() == OsType.OSX) {
          dragboard.setDragView(dragNDrop);
        }

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(DND_TRACK_DATA_FORMAT, track);
        dragboard.setContent(clipboardContent);

        fireEvent(TRACK_SELECTED, track);
      }

      event.consume();
    });

    tableCell.setOnDragOver(event -> {
      if (event.getGestureSource() != tableCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT) &&
          nonNull(tableCell.getTableRow().getItem()) &&
          tableCell.getTableRow().getItem().getTrack().getPlaylistId() != PLAYLIST_ID_SEARCH) {
        event.acceptTransferModes(TransferMode.MOVE);
      }

      event.consume();
    });

    tableCell.setOnDragEntered(event -> {
      if (event.getGestureSource() != tableCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT) &&
          nonNull(tableCell.getTableRow().getItem()) &&
          tableCell.getTableRow().getItem().getTrack().getPlaylistId() != PLAYLIST_ID_SEARCH) {
        tableCell.getTableRow().setStyle("-fx-background-color: -jb-border-color");
      }

      event.consume();
    });

    tableCell.setOnDragExited(event -> {
      tableCell.getTableRow().setStyle(null);

      event.consume();
    });

    tableCell.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();

      if (dragboard.hasContent(DND_TRACK_DATA_FORMAT)) {
        Track source = (Track) dragboard.getContent(DND_TRACK_DATA_FORMAT);
        Track target = tableCell.getTableRow().getItem().getTrack();

        playlistService.moveTracksInPlaylist(source.getPlaylistId(), source, target);

        event.setDropCompleted(true);
      }

      event.consume();
    });

    tableCell.setOnDragDone(Event::consume);

    return tableCell;
  }
}
