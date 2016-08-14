package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@Component
public class EqualizerController extends EventAwareObject {
	
	@FXML
	private HBox sliderHbox;

	@FXML
	private Button okButton;
	
	@FXML
	private Button resetButton;
	
	@Autowired
	private MediaManager mediaManager;

	@FXML
	public void initialize() {
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

	public void updateSliderValues() {
		Equalizer equalizer = mediaManager.getEqualizer();
		
		for (Node node : sliderHbox.getChildren()) {
			final Slider slider = (Slider)node;
			
			slider.setValue(equalizer.getGain(Integer.parseInt(slider.getId().substring(2))));
		}
	}

	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
		FxmlContext.getBean(MainPanelController.class).getEqualizerDialogue().close();
	}
	
	@FXML
	protected void handleResetButtonAction(ActionEvent event) {
		for (Node node : sliderHbox.getChildren()) {
			final Slider slider = (Slider)node;
			
			slider.setValue(0);
			
			fireEvent(Event.EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
		}
	}
}
