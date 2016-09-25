package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.visualiser.impl.EqVisualizer;

@Slf4j
@Component
public class VisualizerController {

	@FXML
	private Pane visualizerPane;
	
	@Autowired
	private MediaManager mediaManager;
	
	public void createVisualizer() {
		log.debug("Creating visualizer");
		
		mediaManager.setVisualizer(new EqVisualizer(visualizerPane));
	}
	
	public void destroyVisualizer() {
		log.debug("Destroying visualizer");
		
		mediaManager.clearVisualiser();
	}
}
