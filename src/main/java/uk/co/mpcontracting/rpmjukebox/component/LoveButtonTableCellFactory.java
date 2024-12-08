package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Optional.ofNullable;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CONTENT_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;

@RequiredArgsConstructor
public class LoveButtonTableCellFactory extends EventAwareObject implements Callback<TableColumn<TrackTableModel, String>, TableCell<TrackTableModel, String>> {

  private final PlaylistService playlistService;

  @Override
  public TableCell<TrackTableModel, String> call(TableColumn<TrackTableModel, String> trackTableModelStringTableColumn) {
    final LoveButtonTableCell tableCell = new LoveButtonTableCell(playlistService);

    //////////////////
    // Mouse Events //
    //////////////////

    tableCell.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY) {
        if (event.getClickCount() == 1) {
          // Single click
          ofNullable(tableCell.getItem()).ifPresent(item -> {
            Track track = tableCell.getTableRow().getItem().getTrack();

            if (playlistService.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, track.getTrackId())) {
              playlistService.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
            } else {
              playlistService.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track.createClone());
            }

            fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
          });
        }
      }
    });

    return tableCell;
  }
}
