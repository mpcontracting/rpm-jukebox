package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import uk.co.mpcontracting.rpmjukebox.support.FxmlHelper;

public class EqualizerDialogue extends Stage {

	public EqualizerDialogue(Stage stage) {
		super(StageStyle.TRANSPARENT);

		initModality(Modality.WINDOW_MODAL);
		initOwner(stage);

		setScene(new Scene((BorderPane)FxmlHelper.loadFxml("equalizer.fxml"), Color.TRANSPARENT));
		
		addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				setX((getOwner().getX() + getOwner().getWidth() / 2) - getWidth() / 2);
				setY((getOwner().getY() + getOwner().getHeight() / 2) - getHeight() / 2);
			}
		});
	}
}
