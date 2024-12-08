package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FXMLController
public class SettingsController {

  @FXML
  private Label versionLabel;

  @FXML
  private TextField cacheSizeMbTextField;

  @FXML
  private TextField proxyHostTextField;

  @FXML
  private TextField proxyPortTextField;

  @FXML
  private CheckBox proxyAuthCheckBox;

  @FXML
  private TextField proxyUsernameTextField;

  @FXML
  private PasswordField proxyPasswordTextField;

  @FXML
  private Button cancelButton;

  void bindSystemSettings() {

  }

  @FXML
  protected void handleReindexButtonAction() {

  }

  @FXML
  protected void handleOkButtonAction() {

  }

  @FXML
  protected void handleCancelButtonAction() {

  }
}
