package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

@Getter
public class PlaylistTableModel {

  private final Playlist playlist;
  private final BooleanProperty selected;
  private final StringProperty name;

  public PlaylistTableModel(Playlist playlist) {
    this.playlist = playlist;

    selected = new SimpleBooleanProperty();
    name = new SimpleStringProperty(playlist.getName());
  }
}
