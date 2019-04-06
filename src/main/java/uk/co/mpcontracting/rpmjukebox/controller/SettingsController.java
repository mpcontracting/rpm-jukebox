package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.support.ValidationHelper;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class SettingsController extends EventAwareObject implements Constants {

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
    private final MessageManager messageManager;

    private SettingsView settingsView;
    private SettingsManager settingsManager;
    private SearchManager searchManager;
    private MainPanelController mainPanelController;

    private boolean isReindexing;

    @Autowired
    private void wireSettingsView(SettingsView settingsView) {
        this.settingsView = settingsView;
    }

    @Autowired
    private void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    private void wireSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    private void wireMainPanelController(MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

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

    void bindSystemSettings() {
        SystemSettings systemSettings = settingsManager.getSystemSettings();

        versionLabel.setText(messageManager.getMessage(MESSAGE_SETTINGS_COPYRIGHT_2, settingsManager.getVersion()));
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
        boolean isFormValid = true;

        // Cache size MB text field
        if (!ValidationHelper.validateIntegerField(cacheSizeMbTextField, true, 50, 1000)) {
            isFormValid = false;
        }

        // Proxy host field
        if (!ValidationHelper.validateTextField(proxyHostTextField,
            !ValidationHelper.nullAsBlank(proxyPortTextField.getText()).isEmpty(), null, 255)) {
            isFormValid = false;
        }

        // Proxy port field
        if (!ValidationHelper.validateIntegerField(proxyPortTextField,
            !ValidationHelper.nullAsBlank(proxyHostTextField.getText()).isEmpty(), 80, 65535)) {
            isFormValid = false;
        }

        // Proxy username field
        if (!ValidationHelper.validateTextField(proxyUsernameTextField,
            ValidationHelper.nullAsFalse(proxyAuthCheckBox.isSelected()), null, 255)) {
            isFormValid = false;
        }

        // Proxy password field
        if (!ValidationHelper.validateTextField(proxyPasswordTextField,
            ValidationHelper.nullAsFalse(proxyAuthCheckBox.isSelected()), null, 255)) {
            isFormValid = false;
        }

        return isFormValid;
    }

    @FXML
    protected void handleReindexButtonAction() {
        log.debug("Re-index data button pressed");

        // Don't run this on the GUI thread
        threadRunner.run(() -> {
            try {
                mainPanelController.showMessageView(messageManager.getMessage(MESSAGE_DOWNLOAD_INDEX), false);
                isReindexing = true;
                searchManager.indexData();
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

        SystemSettings systemSettings = settingsManager.getSystemSettings();
        systemSettings.setCacheSizeMb(Integer.parseInt(cacheSizeMbTextField.getText()));
        systemSettings.setProxyHost(proxyHostTextField.getText());
        systemSettings.setProxyPort(!proxyPortTextField.getText().isEmpty() ? Integer.parseInt(proxyPortTextField.getText()) : null);
        systemSettings.setProxyRequiresAuthentication(proxyAuthCheckBox.isSelected());
        systemSettings.setProxyUsername(proxyUsernameTextField.getText());
        systemSettings.setProxyPassword(proxyPasswordTextField.getText());

        settingsManager.saveSystemSettings();

        settingsView.close();
    }

    @FXML
    protected void handleCancelButtonAction() {
        settingsView.close();
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        if (event == Event.DATA_INDEXED && isReindexing) {
            mainPanelController.closeMessageView();
            isReindexing = false;
        }
    }
}
