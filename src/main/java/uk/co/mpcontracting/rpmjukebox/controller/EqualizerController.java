package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private EqualizerView equalizerView;
    private MediaManager mediaManager;

    @Autowired
    private void wireEqualizerView(EqualizerView equalizerView) {
        this.equalizerView = equalizerView;
    }

    @Autowired
    private void wireMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @FXML
    public void initialize() {
        log.info("Initialising EqualizerController");

        sliderHbox.getChildren().forEach(node -> {
            final Slider slider = (Slider) node;

            slider.valueProperty().addListener(observable -> {
                if (slider.isValueChanging()) {
                    fireEvent(Event.EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)),
                            slider.getValue());
                }
            });
        });
    }

    void updateSliderValues() {
        Equalizer equalizer = mediaManager.getEqualizer();

        sliderHbox.getChildren().forEach(node -> {
            final Slider slider = (Slider) node;

            slider.setValue(equalizer.getGain(Integer.parseInt(slider.getId().substring(2))));
        });
    }

    @FXML
    protected void handleOkButtonAction() {
        equalizerView.close();
    }

    @FXML
    protected void handleResetButtonAction() {
        sliderHbox.getChildren().forEach(node -> {
            final Slider slider = (Slider) node;

            slider.setValue(0);

            fireEvent(Event.EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
        });
    }
}
