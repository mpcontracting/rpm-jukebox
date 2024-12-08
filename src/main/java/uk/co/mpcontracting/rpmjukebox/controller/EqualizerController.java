package uk.co.mpcontracting.rpmjukebox.controller;

import static uk.co.mpcontracting.rpmjukebox.event.Event.EQUALIZER_UPDATED;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.service.MediaService;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class EqualizerController extends EventAwareObject {

  @FXML
  private HBox sliderHbox;

  private final EqualizerView equalizerView;
  private final MediaService mediaService;

  @FXML
  public void initialize() {
    log.info("Initialising EqualizerController");

    sliderHbox.getChildren().forEach(node -> {
      final Slider slider = (Slider) node;

      slider.valueProperty().addListener(observable -> {
        if (slider.isValueChanging()) {
          fireEvent(EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
        }
      });
    });
  }

  protected void updateSliderValues() {
    Equalizer equalizer = mediaService.getEqualizer();

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

      fireEvent(EQUALIZER_UPDATED, Integer.parseInt(slider.getId().substring(2)), slider.getValue());
    });
  }
}
