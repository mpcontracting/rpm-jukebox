package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@Slf4j
@Component
public class SettingsController {

	@FXML
	public void initialize() {
		log.info("Initialising SettingsController");
	}
	
	@FXML
	protected void handleCancelButtonAction(ActionEvent event) {
		FxmlContext.getBean(MainPanelController.class).getSettingsWindow().close();
	}
}
