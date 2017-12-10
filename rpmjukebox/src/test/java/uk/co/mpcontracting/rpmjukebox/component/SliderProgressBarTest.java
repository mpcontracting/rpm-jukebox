package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.scene.control.ProgressBar;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class SliderProgressBarTest extends AbstractTest {

    @Test
    public void shouldGetSliderValueProperty() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        
        assertThat("Slider value property should not be null", sliderProgressBar.sliderValueProperty(), notNullValue());
    }
    
    @Test
    public void shouldGetSliderValueChangingProperty() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        
        assertThat("Slider value changing property should not be null", sliderProgressBar.sliderValueChangingProperty(), notNullValue());
    }
    
    @Test
    public void shouldGetIsSliderValueChanging() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        
        assertThat("Is slider value changing should be false", sliderProgressBar.isSliderValueChanging(), equalTo(false));
    }

    @Test
    public void shouldGetSliderValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        
        assertThat("Slider value should be 0.0", sliderProgressBar.getSliderValue(), equalTo(0.0d));
    }
    
    @Test
    public void shouldSetSliderValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        sliderProgressBar.setSliderValue(0.5);
        
        assertThat("Slider value should be 0.5", sliderProgressBar.getSliderValue(), equalTo(0.5d));
    }
    
    @Test
    public void shouldSetProgressValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        sliderProgressBar.setProgressValue(50);
        
        double progress = ((ProgressBar)ReflectionTestUtils.getField(sliderProgressBar, "progressBar")).getProgress();
        
        assertThat("Progress value should be 0.5", progress, equalTo(0.5d));
    }
}
