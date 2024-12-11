package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.isNull;

import javafx.scene.control.TableCell;

public class PlaylistTableCell<T> extends TableCell<PlaylistTableModel, T> {

  @Override
  protected void updateItem(T value, boolean empty) {
    super.updateItem(value, empty);

    setGraphic(null);

    if (empty || isNull(value)) {
      setText(null);
    } else {
      setText(value.toString());
    }
  }
}
