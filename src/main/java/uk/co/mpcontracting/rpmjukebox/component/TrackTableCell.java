package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.isNull;

import javafx.scene.control.TableCell;

public class TrackTableCell<T> extends TableCell<TrackTableModel, T> {

  @Override
  protected void updateItem(T value, boolean empty) {
    super.updateItem(value, empty);

    if (empty || isNull(value)) {
      setText(null);
    } else {
      setText(value.toString());
    }
  }
}
