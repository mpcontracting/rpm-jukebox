package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;

@Slf4j
@Component
public class TrackTableController {

	@FXML
	private TableView<TrackTableModel> trackTable;
	
	@FXML
	private TableColumn<TrackTableModel, Boolean> isPreferredColumn;
	
	@FXML
	private TableColumn<TrackTableModel, Integer> trackNumberColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> trackNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> artistNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, Integer> albumYearColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> albumNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> genresColumn;
	
	@FXML
	public void initialize() {
		log.info("Initialising TrackTableController");
		
		trackTable.setPlaceholder(new Label(""));
	}
}
