package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.effect.BoxBlur;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ModalWindow extends Stage {

	private boolean blurBackground;
	
	public ModalWindow(Stage stage, String fxmlFile) {
		super(StageStyle.TRANSPARENT);

		initModality(Modality.WINDOW_MODAL);
		initOwner(stage);

		//setScene(new Scene((Parent)FxmlContext.loadFxml(fxmlFile), Color.TRANSPARENT));
		
		addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
			setX((getOwner().getX() + getOwner().getWidth() / 2) - getWidth() / 2);
			setY((getOwner().getY() + getOwner().getHeight() / 2) - getHeight() / 2);
		});
	}

	public void display(boolean blurBackground) {
		this.blurBackground = blurBackground;
		
		if (blurBackground) {
			getOwner().getScene().getRoot().setEffect(new BoxBlur());
		}
		
		show();
	}
	
	@Override
	public void close() {
		if (blurBackground) {
			getOwner().getScene().getRoot().setEffect(null);
		}
		
		super.close();
	}
}
