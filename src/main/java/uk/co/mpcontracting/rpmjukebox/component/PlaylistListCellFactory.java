package uk.co.mpcontracting.rpmjukebox.component;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.DND_TRACK_DATA_FORMAT;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_CONTEXT_DELETE_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_CONTEXT_NEW_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;

@RequiredArgsConstructor
public class PlaylistListCellFactory extends EventAwareObject implements Callback<ListView<Playlist>, ListCell<Playlist>> {

  private final StringResourceService stringResourceService;
  private final PlaylistService playlistService;

  @Override
  public ListCell<Playlist> call(ListView<Playlist> listView) {
    final PlaylistListCell listCell = new PlaylistListCell(new PlaylistStringConverter<>());

    //////////////////
    // Context Menu //
    //////////////////

    ContextMenu contextMenu = new ContextMenu();

    final MenuItem newPlaylistItem = new MenuItem(stringResourceService.getString(MESSAGE_PLAYLIST_CONTEXT_NEW_PLAYLIST));
    newPlaylistItem.setOnAction(event -> playlistService.createPlaylist());
    contextMenu.getItems().add(newPlaylistItem);

    final MenuItem deletePlaylistItem = new MenuItem(stringResourceService.getString(MESSAGE_PLAYLIST_CONTEXT_DELETE_PLAYLIST));
    deletePlaylistItem.setOnAction(event -> {
      Playlist playlist = listView.getSelectionModel().getSelectedItem();

      getApplicationContext().getBean(MainPanelController.class)
          .showConfirmView(stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()),
              true, () -> playlistService.deletePlaylist(playlist.getPlaylistId()), null);
    });
    contextMenu.getItems().add(deletePlaylistItem);

    listCell.setContextMenu(contextMenu);
    listCell.setOnContextMenuRequested(event -> {
      @SuppressWarnings("unchecked")
      ListCell<Playlist> sourceCell = (ListCell<Playlist>) event.getSource();

      if (listView.getItems().size() > sourceCell.getIndex()) {
        Playlist playlist = listView.getItems().get(sourceCell.getIndex());

        deletePlaylistItem.setDisable(playlist.getPlaylistId() < 0);
        newPlaylistItem.setDisable(true);
      } else {
        newPlaylistItem.setDisable(false);
        deletePlaylistItem.setDisable(true);
      }
    });

    ///////////////////
    // Drag And Drop //
    ///////////////////

    listCell.setOnDragOver(event -> {
      if (event.getGestureSource() != listCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
        event.acceptTransferModes(TransferMode.COPY);
      }

      event.consume();
    });

    listCell.setOnDragEntered(event -> {
      if (event.getGestureSource() != listCell && listCell.getItem() != null &&
          listCell.getItem().getPlaylistId() > -1 && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
        listCell.setStyle("-fx-background-color: -jb-foreground-color; -fx-text-fill: -jb-background-color");
      }

      event.consume();
    });

    listCell.setOnDragExited(event -> {
      listCell.setStyle(null);

      event.consume();
    });

    listCell.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();

      if (dragboard.hasContent(DND_TRACK_DATA_FORMAT)) {
        playlistService.addTrackToPlaylist(listCell.getItem().getPlaylistId(),
            ((Track) dragboard.getContent(DND_TRACK_DATA_FORMAT)).createClone());

        event.setDropCompleted(true);
      }

      event.consume();
    });

    return listCell;
  }
}
