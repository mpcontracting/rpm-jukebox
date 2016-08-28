package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;

@Slf4j
@Component
public class SettingsController extends EventAwareObject {

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
