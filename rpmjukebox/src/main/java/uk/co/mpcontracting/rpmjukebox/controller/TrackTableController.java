package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;

@Slf4j
@Component
public class TrackTableController {

	@FXML
	public void initialize() {
		log.info("Initialising TrackTableController");
	}
}
