package uk.co.mpcontracting.rpmjukebox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class SettingsController extends EventAwareObject implements Constants {

	@FXML
	private Label versionLabel;
	
	@FXML
	private TextField cacheSizeMbTextField;
	
	@FXML
	private Button cancelButton;
	
	@Autowired
	private MessageManager messageManager;
	
	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private MainPanelController mainPanelController;
	
	private boolean isReindexing;
	
	@FXML
	public void initialize() {
		log.info("Initialising SettingsController");
		
		isReindexing = false;

		cacheSizeMbTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				validate();
			}
		});
	}
	
	public void bindSystemSettings() {
		SystemSettings systemSettings = settingsManager.getSystemSettings();

		versionLabel.setText(messageManager.getMessage(MESSAGE_SETTINGS_COPYRIGHT_2, settingsManager.getVersion()));
		cacheSizeMbTextField.setText(Integer.toString(systemSettings.getCacheSizeMb()));
		cancelButton.requestFocus();
		
		validate();
	}
	
	private boolean validate() {
		boolean isFormValid = true;
		
		// Cache size MB text field
		boolean isCacheSizeMbValid = false;
		
		try {
			String cacheSizeMbText = cacheSizeMbTextField.getText();
			
			if (cacheSizeMbText != null && cacheSizeMbText.trim().length() > 0) {
				int cacheSizeMb = Integer.parseInt(cacheSizeMbTextField.getText());
				
				if (cacheSizeMb >= 50 && cacheSizeMb <= 1000) {
					isCacheSizeMbValid = true;
				}
			}
		} catch (Exception e) {}
		
		if (!isCacheSizeMbValid) {
			cacheSizeMbTextField.setStyle(STYLE_INVALID_BORDER);
			isFormValid = false;
		} else {
			cacheSizeMbTextField.setStyle(STYLE_VALID_BORDER);
		}
		
		return isFormValid;
	}
	
	@FXML
	protected void handleReindexButtonAction(ActionEvent event) {
		log.debug("Re-index data button pressed");

		// Don't run this on the GUI thread
		ThreadRunner.run(() -> {
			try {
				isReindexing = true;
				searchManager.indexData(false);
			} catch (Exception e) {
				mainPanelController.closeMessageWindow();
				isReindexing = false;
			}
		});
	}
	
	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
		if (!validate()) {
			return;
		}
		
		SystemSettings systemSettings = settingsManager.getSystemSettings();
		
		systemSettings.setCacheSizeMb(Integer.parseInt(cacheSizeMbTextField.getText()));
		
		mainPanelController.getSettingsWindow().close();
	}
	
	@FXML
	protected void handleCancelButtonAction(ActionEvent event) {
		mainPanelController.getSettingsWindow().close();
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case DATA_INDEXED: {
				if (isReindexing) {
					mainPanelController.closeMessageWindow();
					isReindexing = false;
				}
				
				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
