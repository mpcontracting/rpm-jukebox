package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.model.SystemSettings;

@Slf4j
@Component
public class SettingsController extends EventAwareObject {

	@FXML
	private TextField defaultVolumeTextField;
	
	@FXML
	private TextField maxSearchHitsTextField;
	
	@FXML
	private TextField maxPlaylistSizeTextField;
	
	@FXML
	private TextField randomPlaylistSizeTextField;
	
	@FXML
	private TextField cacheSizeMbTextField;
	
	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private MainPanelController mainPanelController;
	
	private boolean isReindexing;
	
	@FXML
	public void initialize() {
		log.info("Initialising SettingsController");
		
		isReindexing = false;
	}
	
	public void bindSystemSettings(SystemSettings systemSettings) {
		log.info("Binding system settings - " + systemSettings);
		
		defaultVolumeTextField.setText(Double.toString(systemSettings.getDefaultVolume()));
		maxSearchHitsTextField.setText(Integer.toString(systemSettings.getMaxSearchHits()));
		maxPlaylistSizeTextField.setText(Integer.toString(systemSettings.getMaxPlaylistSize()));
		randomPlaylistSizeTextField.setText(Integer.toString(systemSettings.getRandomPlaylistSize()));
		cacheSizeMbTextField.setText(Integer.toString(systemSettings.getCacheSizeMb()));
	}
	
	@FXML
	protected void handleReindexButtonAction(ActionEvent event) {
		log.info("Re-index data button pressed");

		// Don't run this on the GUI thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					isReindexing = true;
					searchManager.indexData(false);
				} catch (Exception e) {
					mainPanelController.closeMessageWindow();
					isReindexing = false;
				}
			}
		}).start();
	}
	
	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
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
