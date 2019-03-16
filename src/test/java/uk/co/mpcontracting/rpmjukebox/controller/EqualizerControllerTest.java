package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.scene.control.Slider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;

import javax.annotation.PostConstruct;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class EqualizerControllerTest extends AbstractTest {

    @Autowired
    private EqualizerController equalizerController;

    @Autowired
    private EqualizerView equalizerView;

    @Mock
    private MediaManager mockMediaManager;

    private EqualizerView spyEqualizerView;

    @PostConstruct
    public void constructView() throws Exception {
        spyEqualizerView = spy(equalizerView);

        setField(equalizerController, "equalizerView", spyEqualizerView);

        init(spyEqualizerView);
    }

    @Before
    public void setup() {
        setField(equalizerController, "eventManager", getMockEventManager());
        setField(equalizerController, "mediaManager", mockMediaManager);

        doNothing().when(spyEqualizerView).close();
    }

    @Test
    public void shouldUpdateSliderValues() {
        Equalizer equalizer = new Equalizer(10);

        for (int i = 0; i < 10; i++) {
            equalizer.setGain(i, 0.5d);
        }

        when(mockMediaManager.getEqualizer()).thenReturn(equalizer);

        equalizerController.updateSliderValues();

        for (int i = 0; i < 10; i++) {
            Slider slider = find("#eq" + i);

            assertThat("Slider " + i + " should have a value of 0.5", slider.getValue(), equalTo(0.5d));
        }
    }

    @Test
    public void shouldClickOkButton() {
        clickOn("#okButton");

        verify(spyEqualizerView, times(1)).close();
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
