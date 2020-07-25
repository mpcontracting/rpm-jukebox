package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

import static java.util.Optional.ofNullable;

public class LoveButtonTableCellFactory extends EventAwareObject implements Callback<TableColumn<TrackTableModel, String>, TableCell<TrackTableModel, String>>, Constants {

    private final PlaylistManager playlistManager;

    public LoveButtonTableCellFactory() {
        playlistManager = ContextHelper.getBean(PlaylistManager.class);
    }

    @Override
    public TableCell<TrackTableModel, String> call(TableColumn<TrackTableModel, String> tableColumn) {
        final LoveButtonTableCell tableCell = new LoveButtonTableCell(playlistManager);

        //////////////////
        // Mouse Events //
        //////////////////

        tableCell.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getClickCount() == 1) {
                    // Single click
                    ofNullable(tableCell.getItem()).ifPresent(item -> {
                        Track track = tableCell.getTableRow().getItem().getTrack();

                        if (playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, track.getTrackId())) {
                            playlistManager.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
                        } else {
                            playlistManager.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track.clone());
                        }

                        fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
                    });
                }
            }
        });

        return tableCell;
    }
}
