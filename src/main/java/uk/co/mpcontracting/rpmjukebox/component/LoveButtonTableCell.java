package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.nonNull;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_LOVE_BUTTON_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_LOVE_BUTTON_ON;

import javafx.scene.control.TableCell;
import lombok.RequiredArgsConstructor;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;

@RequiredArgsConstructor
public class LoveButtonTableCell extends TableCell<TrackTableModel, String> {

  private final PlaylistService playlistService;

  @Override
  protected void updateItem(String value, boolean empty) {
    super.updateItem(value, empty);

    setText(null);
    setGraphic(null);

    if (!empty && nonNull(value)) {
      if (playlistService.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, value)) {
        setId(STYLE_LOVE_BUTTON_ON);
      } else {
        setId(STYLE_LOVE_BUTTON_OFF);
      }
    } else {
      setId(null);
    }
  }
}
