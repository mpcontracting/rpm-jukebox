package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

public class SliderProgressBar extends StackPane {

    private ProgressBar progressBar;
    private Slider slider;

    public SliderProgressBar() {
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);

        getChildren().addAll(progressBar, slider);
    }

    public DoubleProperty sliderValueProperty() {
        return slider.valueProperty();
    }

    public BooleanProperty sliderValueChangingProperty() {
        return slider.valueChangingProperty();
    }

    public boolean isSliderValueChanging() {
        return slider.isValueChanging();
    }

    public void setSliderValue(double value) {
        slider.setValue(value);
    }

    public double getSliderValue() {
        return slider.getValue();
    }

    public void setProgressValue(double value) {
        progressBar.setProgress(value / 100);
    }

    public double getProgressValue() {
        return progressBar.getProgress();
    }
}
