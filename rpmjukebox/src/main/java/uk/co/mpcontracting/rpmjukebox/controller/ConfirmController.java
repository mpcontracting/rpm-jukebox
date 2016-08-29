package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;

@Slf4j
@Component
public class ConfirmController {

	@FXML
	private Button okButton;
	
	@FXML
	private Button cancelButton;
	
	@Autowired
	private MainPanelController mainPanelController;

	private Runnable okRunnable;
	private Runnable cancelRunnable;
	
	public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
		this.okRunnable = okRunnable;
		this.cancelRunnable = cancelRunnable;
	}
	
	public void setOkFocused() {
		okButton.requestFocus();
	}
	
	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
		okButtonPressed();
	}
	
	@FXML
	protected void handleOkButtonKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			okButtonPressed();
		}
	}
	
	private void okButtonPressed() {
		log.debug("OK button pressed");
		
		if (okRunnable != null) {
			okRunnable.run();
		}

		mainPanelController.closeConfirmWindow();
	}
	
	@FXML
	protected void handleCancelButtonAction(ActionEvent event) {
		cancelButtonPressed();
	}
	
	@FXML
	protected void handleCancelButtonKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			cancelButtonPressed();
		}
	}
	
	private void cancelButtonPressed() {
		log.debug("Cancel button pressed");
		
		if (cancelRunnable != null) {
			cancelRunnable.run();
		}

		mainPanelController.closeConfirmWindow();
	}
}
