package uk.co.mpcontracting.rpmjukebox.controller;

import static uk.co.mpcontracting.rpmjukebox.event.Event.DATA_INDEXED;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_DOWNLOAD_INDEX;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SETTINGS_COPYRIGHT_2;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.service.SearchService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.util.ValidationHelper;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class SettingsController extends EventAwareObject {

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

  private final ThreadRunner threadRunner;

  private final SettingsView settingsView;
  private final SearchService searchService;
  private final SettingsService settingsService;
  private final StringResourceService stringResourceService;

  @Lazy
  @Autowired
  private MainPanelController mainPanelController;

  private boolean isReindexing;

  @FXML
  public void initialize() {
    log.info("Initialising SettingsController");

    isReindexing = false;

    addFocusListener(cacheSizeMbTextField);
    addFocusListener(proxyHostTextField);
    addFocusListener(proxyPortTextField);
    addFocusListener(proxyAuthCheckBox);
    addFocusListener(proxyUsernameTextField);
    addFocusListener(proxyPasswordTextField);
  }

  private void addFocusListener(Control control) {
    control.focusedProperty().addListener((observable, oldValue, newValue) -> focusChanged(newValue));
  }

  private void focusChanged(boolean newValue) {
    if (!newValue) {
      trimFields();
      validate();
    }
  }

  private void trimFields() {
    cacheSizeMbTextField.setText(ValidationHelper.nullAsBlank(cacheSizeMbTextField.getText()));
    proxyHostTextField.setText(ValidationHelper.nullAsBlank(proxyHostTextField.getText()));
    proxyPortTextField.setText(ValidationHelper.nullAsBlank(proxyPortTextField.getText()));
    proxyUsernameTextField.setText(ValidationHelper.nullAsBlank(proxyUsernameTextField.getText()));
    proxyPasswordTextField.setText(ValidationHelper.nullAsBlank(proxyPasswordTextField.getText()));
  }

  private boolean validate() {
    boolean cacheSizeValid = ValidationHelper.validateIntegerField(cacheSizeMbTextField, true, 50, 1000);
    boolean proxyHostValid = ValidationHelper.validateTextField(proxyHostTextField, !ValidationHelper.nullAsBlank(proxyPortTextField.getText()).isEmpty(), null, 255);
    boolean proxyPortValid = ValidationHelper.validateIntegerField(proxyPortTextField, !ValidationHelper.nullAsBlank(proxyHostTextField.getText()).isEmpty(), 80, 65535);
    boolean proxyUsernameValid = ValidationHelper.validateTextField(proxyUsernameTextField, ValidationHelper.nullAsFalse(proxyAuthCheckBox.isSelected()), null, 255);
    boolean proxyPasswordValid = ValidationHelper.validateTextField(proxyPasswordTextField, ValidationHelper.nullAsFalse(proxyAuthCheckBox.isSelected()), null, 255);

    return cacheSizeValid && proxyHostValid && proxyPortValid && proxyUsernameValid && proxyPasswordValid;
  }

  void bindSystemSettings() {
    SystemSettings systemSettings = settingsService.getSystemSettings();

    versionLabel.setText(stringResourceService.getString(MESSAGE_SETTINGS_COPYRIGHT_2, settingsService.getVersion()));
    cacheSizeMbTextField.setText(ValidationHelper.nullAsBlank(systemSettings.getCacheSizeMb()));
    proxyHostTextField.setText(ValidationHelper.nullAsBlank(systemSettings.getProxyHost()));
    proxyPortTextField.setText(ValidationHelper.nullAsBlank((systemSettings.getProxyPort())));
    proxyAuthCheckBox.setSelected(ValidationHelper.nullAsFalse(systemSettings.getProxyRequiresAuthentication()));
    proxyUsernameTextField.setText(ValidationHelper.nullAsBlank(systemSettings.getProxyUsername()));
    proxyPasswordTextField.setText(ValidationHelper.nullAsBlank(systemSettings.getProxyPassword()));
    cancelButton.requestFocus();

    trimFields();
    validate();
  }

  @FXML
  protected void handleReindexButtonAction() {
    log.debug("Re-index data button pressed");

    // Don't run this on the GUI thread
    threadRunner.run(() -> {
      try {
        mainPanelController.showMessageView(stringResourceService.getString(MESSAGE_DOWNLOAD_INDEX), false);
        isReindexing = true;
        searchService.indexData();
      } catch (Exception e) {
        mainPanelController.closeMessageView();
        isReindexing = false;
      }
    });
  }

  @FXML
  protected void handleOkButtonAction() {
    trimFields();

    if (!validate()) {
      return;
    }

    SystemSettings systemSettings = settingsService.getSystemSettings();
    systemSettings.setCacheSizeMb(Integer.parseInt(cacheSizeMbTextField.getText()));
    systemSettings.setProxyHost(proxyHostTextField.getText());
    systemSettings.setProxyPort(!proxyPortTextField.getText().isEmpty() ? Integer.parseInt(proxyPortTextField.getText()) : null);
    systemSettings.setProxyRequiresAuthentication(proxyAuthCheckBox.isSelected());
    systemSettings.setProxyUsername(proxyUsernameTextField.getText());
    systemSettings.setProxyPassword(proxyPasswordTextField.getText());

    settingsService.saveSystemSettings();

    settingsView.close();
  }

  @FXML
  protected void handleCancelButtonAction() {
    settingsView.close();
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    if (event == DATA_INDEXED && isReindexing) {
      mainPanelController.closeMessageView();
      isReindexing = false;
    }
  }
}
