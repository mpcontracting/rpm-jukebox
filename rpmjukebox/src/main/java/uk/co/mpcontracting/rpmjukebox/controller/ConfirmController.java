package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;

@Slf4j
@Component
public class ConfirmController {

	@FXML
	private Button okButton;
	
	@FXML
	private Button cancelButton;

	private Runnable okRunnable;
	private Runnable cancelRunnable;
	
	public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
		this.okRunnable = okRunnable;
		this.cancelRunnable = cancelRunnable;
		
		if (okRunnable != null) {
			okButton.setVisible(true);
		} else {
			okButton.setVisible(false);
		}
		
		if (cancelRunnable != null) {
			cancelButton.setVisible(true);
		} else {
			cancelButton.setVisible(false);
		}
	}
	
	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
		log.debug("OK button pressed");
		
		if (okRunnable != null) {
			okRunnable.run();
		}
		
		((Stage)okButton.getScene().getWindow()).close();
	}
	
	@FXML
	protected void handleCancelButtonAction(ActionEvent event) {
		log.debug("Cancel button pressed");
		
		if (cancelRunnable != null) {
			cancelRunnable.run();
		}
		
		((Stage)cancelButton.getScene().getWindow()).close();
	}
}
