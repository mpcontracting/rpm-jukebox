package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

public class ModalWindow extends Stage {

	public ModalWindow(Stage stage, String fxmlFile) {
		super(StageStyle.TRANSPARENT);

		initModality(Modality.WINDOW_MODAL);
		initOwner(stage);

		setScene(new Scene((Parent)FxmlContext.loadFxml(fxmlFile), Color.TRANSPARENT));
		
		addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				setX((getOwner().getX() + getOwner().getWidth() / 2) - getWidth() / 2);
				setY((getOwner().getY() + getOwner().getHeight() / 2) - getHeight() / 2);
			}
		});
	}

	public void display() {
		getOwner().getScene().getRoot().setEffect(new BoxBlur());
		show();
	}
	
	@Override
	public void close() {
		getOwner().getScene().getRoot().setEffect(null);
		super.close();
	}
}
