package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

public class MessageWindow extends ModalWindow {

	public MessageWindow(Stage stage, String fxmlFile) {
		super(stage, fxmlFile);
	}

	public void setMessage(String message) {
		FxmlContext.lookup(getScene().getRoot(), "message", Label.class).setText(message);
	}
}
