package uk.co.mpcontracting.rpmjukebox.component;

import javafx.stage.Stage;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

public class ConfirmWindow extends MessageWindow {

	private ConfirmController confirmController;
	
	public ConfirmWindow(Stage stage, String fxmlFile) {
		super(stage, fxmlFile);
		
		confirmController = FxmlContext.getBean(ConfirmController.class);
	}
	
	public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
		confirmController.setRunnables(okRunnable, cancelRunnable);
	}
	
	@Override
	public void display(boolean blurBackground) {
		confirmController.setOkFocused();
		
		super.display(blurBackground);
	}
}
