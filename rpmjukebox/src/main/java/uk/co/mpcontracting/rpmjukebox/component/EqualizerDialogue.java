package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.co.mpcontracting.rpmjukebox.support.FxmlHelper;

public class EqualizerDialogue extends Stage {
	
	public EqualizerDialogue(Stage stage) {
		super(StageStyle.TRANSPARENT);

		initModality(Modality.WINDOW_MODAL);
		initOwner(stage);

		setScene(new Scene((BorderPane)FxmlHelper.loadFxml("equalizerDialog.fxml"), Color.TRANSPARENT));
	}
}
