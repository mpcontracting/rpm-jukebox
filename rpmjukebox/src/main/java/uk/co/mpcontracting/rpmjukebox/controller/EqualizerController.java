package uk.co.mpcontracting.rpmjukebox.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@Component
public class EqualizerController extends EventAwareObject implements Initializable {
	
	@FXML
	private HBox sliderHbox;

	@FXML
	private Button ok;
	
	@FXML
	private Button reset;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		for (Node node : sliderHbox.getChildren()) {
			final Slider slider = (Slider)node;

			slider.valueProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					if (slider.isValueChanging()) {
						fireEvent(Event.EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
					}
				}
			});
		}
	}

	@FXML
	protected void handleOkAction(ActionEvent event) {
		FxmlContext.getBean(RpmJukebox.class).getStage().getScene().getRoot().setEffect(null);
		FxmlContext.getBean(MainPanelController.class).getEqualizerDialogue().close();
	}
	
	@FXML
	protected void handleResetAction(ActionEvent event) {
		
	}
}
