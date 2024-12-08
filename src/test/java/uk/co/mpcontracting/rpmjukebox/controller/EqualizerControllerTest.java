package uk.co.mpcontracting.rpmjukebox.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.EQUALIZER_UPDATED;

import jakarta.annotation.PostConstruct;
import javafx.scene.control.Slider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.service.MediaService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;

class EqualizerControllerTest extends AbstractGuiTest {

  @MockBean
  private MediaService mediaService;

  @SpyBean
  private EqualizerView equalizerView;

  @Autowired
  private EqualizerController underTest;

  @SneakyThrows
  @PostConstruct
  public void postConstruct() {
    init(equalizerView);
  }

  @BeforeEach
  void beforeEach() {
    doNothing().when(equalizerView).close();
  }

  @Test
  public void shouldUpdateSliderValues() {
    Equalizer equalizer = new Equalizer(10);

    for (int i = 0; i < 10; i++) {
      equalizer.setGain(i, 0.5d);
    }

    when(mediaService.getEqualizer()).thenReturn(equalizer);

    underTest.updateSliderValues();

    for (int i = 0; i < 10; i++) {
      Slider slider = find("#eq" + i);

      assertThat(slider.getValue()).isEqualTo(0.5d);
    }
  }

  @Test
  public void shouldClickOkButton() {
    clickOn("#okButton");

    verify(equalizerView).close();
  }

  @Test
  public void shouldClickResetButton() {
    clickOn("#resetButton");

    for (int i = 0; i < 10; i++) {
      verify(eventProcessor).fireEvent(EQUALIZER_UPDATED, i, 0.0d);
    }
  }

  @Test
  public void shouldMoveAndUpdateEqBand() {
    Equalizer equalizer = new Equalizer(10);

    for (int i = 0; i < 10; i++) {
      equalizer.setGain(i, 0.0d);
    }

    when(mediaService.getEqualizer()).thenReturn(equalizer);

    underTest.updateSliderValues();

    Slider slider = find("#eq0");
    slider.valueChangingProperty().set(true);
    slider.valueProperty().set(0.5d);

    verify(eventProcessor, atLeastOnce()).fireEvent(EQUALIZER_UPDATED, 0, 0.5d);
  }
}