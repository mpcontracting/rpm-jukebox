package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableModel;

@Slf4j
@FXMLController
public class ExportController {

  @FXML
  private TableView<PlaylistTableModel> playlistTableView;

  @FXML
  private TableColumn<PlaylistTableModel, Boolean> selectColumn;

  @FXML
  private TableColumn<PlaylistTableModel, String> playlistColumn;

  @FXML
  private Button cancelButton;

  @FXML
  protected void handleOkButtonAction() {

  }

  @FXML
  protected void handleCancelButtonAction() {

  }
}
