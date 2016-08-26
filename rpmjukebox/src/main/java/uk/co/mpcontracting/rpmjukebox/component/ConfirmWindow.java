package uk.co.mpcontracting.rpmjukebox.component;

import javafx.stage.Stage;
import uk.co.mpcontracting.ioc.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;

public class ConfirmWindow extends MessageWindow {

	public ConfirmWindow(Stage stage, String fxmlFile) {
		super(stage, fxmlFile);
	}
	
	public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
		ApplicationContext.getBean(ConfirmController.class).setRunnables(okRunnable, cancelRunnable);
	}
}
