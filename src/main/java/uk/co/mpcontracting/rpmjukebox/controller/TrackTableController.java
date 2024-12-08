package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableView;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@FXMLController
public class TrackTableController {

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

  public Track getSelectedTrack() {
    return null;
  }
}
