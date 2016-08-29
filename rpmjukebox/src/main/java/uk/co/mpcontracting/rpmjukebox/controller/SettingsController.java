package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class SettingsController extends EventAwareObject {

	private static final String VALID_STYLE		= "-fx-border-color: -jb-border-color";
	private static final String INVALID_STYLE	= "-fx-border-color: -jb-error-color";
	
	@FXML
	private TextField cacheSizeMbTextField;
	
	@FXML
	private Button cancelButton;
	
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
			cacheSizeMbTextField.setStyle(INVALID_STYLE);
			isFormValid = false;
		} else {
			cacheSizeMbTextField.setStyle(VALID_STYLE);
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
