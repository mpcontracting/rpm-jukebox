package uk.co.mpcontracting.rpmjukebox.visualizer;

import javafx.scene.layout.Pane;
import javafx.scene.media.AudioSpectrumListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractVisualizer implements AudioSpectrumListener {

	protected abstract void paneWidthChanged(double width);
	protected abstract void paneHeightChanged(double height);
	protected abstract void drawFrame(double timestamp, double duration, float[] magnitudes, float[] phases);
	
	protected AbstractVisualizer(Pane pane) {
		log.info("Initialising AbstractVisualizer - " + getClass().getName());

		pane.widthProperty().addListener((observable, oldValue, newValue) -> {
			paneWidthChanged(newValue.doubleValue());
		});
		
		pane.heightProperty().addListener((observable, oldValue, newValue) -> {
			paneHeightChanged(newValue.doubleValue());
		});
	}
	
	@Override
	public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
		drawFrame(timestamp, duration, magnitudes, phases);
	}
}
