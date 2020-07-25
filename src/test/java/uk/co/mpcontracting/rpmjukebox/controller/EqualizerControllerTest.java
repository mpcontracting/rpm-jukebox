package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.scene.control.Slider;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class EqualizerControllerTest extends AbstractGUITest {

    @Autowired
    private EqualizerController underTest;

    @Autowired
    private EqualizerView originalEqualizerView;

    @Mock
    private MediaManager mediaManager;

    private EqualizerView equalizerView;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        equalizerView = spy(originalEqualizerView);

        setField(underTest, "equalizerView", equalizerView);

        init(equalizerView);
    }

    @Before
    public void setup() {
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "mediaManager", mediaManager);

        doNothing().when(equalizerView).close();
    }

    @Test
    public void shouldUpdateSliderValues() {
        Equalizer equalizer = new Equalizer(10);

        for (int i = 0; i < 10; i++) {
            equalizer.setGain(i, 0.5d);
        }

        when(mediaManager.getEqualizer()).thenReturn(equalizer);

        underTest.updateSliderValues();

        for (int i = 0; i < 10; i++) {
            Slider slider = find("#eq" + i);

            assertThat(slider.getValue()).isEqualTo(0.5d);
        }
    }

    @Test
    public void shouldClickOkButton() {
        clickOn("#okButton");

        verify(equalizerView, times(1)).close();
    }

    @Test
    public void shouldClickResetButton() {
        clickOn("#resetButton");

        for (int i = 0; i < 10; i++) {
            verify(getMockEventManager(), times(1)).fireEvent(Event.EQUALIZER_UPDATED, i, 0.0d);
        }
    }

    @Test
    public void shouldMoveAndUpdateEqBand() {
        Slider slider = find("#eq0");
        slider.valueChangingProperty().set(true);
        slider.valueProperty().set(0.5d);

        verify(getMockEventManager(), atLeastOnce()).fireEvent(Event.EQUALIZER_UPDATED, 0, 0.5d);
    }
}
