package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
public class TrackTableView extends TableView<TrackTableModel> {

  public void highlightTrack(Track track) {
    log.debug("Highlighting track - {}", track);

    ofNullable(track).ifPresent(presentTrack ->
        ofNullable(getItems()).ifPresent(observableTracks -> {
          for (int i = 0; i < observableTracks.size(); i++) {
            TrackTableModel trackTableModel = observableTracks.get(i);

            if (nonNull(trackTableModel.getTrack()) && trackTableModel.getTrack().equals(presentTrack)) {
              getSelectionModel().select(i);
              getFocusModel().focus(i);

              break;
            }
          }
        })
    );
  }
}
