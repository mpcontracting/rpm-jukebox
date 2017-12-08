package uk.co.mpcontracting.rpmjukebox.controller;

import org.springframework.beans.factory.annotation.Autowired;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;

@Slf4j
@FXMLController
public class EqualizerController extends EventAwareObject {
	
	@FXML
	private HBox sliderHbox;
	
	@Autowired
	private EqualizerView equalizerView;
	
	@Autowired
	private MediaManager mediaManager;

	@FXML
	public void initialize() {
		log.info("Initialising EqualizerController");
		
		for (Node node : sliderHbox.getChildren()) {
			final Slider slider = (Slider)node;

			slider.valueProperty().addListener(observable -> {
				if (slider.isValueChanging()) {
					fireEvent(Event.EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
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
		equalizerView.close();
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
