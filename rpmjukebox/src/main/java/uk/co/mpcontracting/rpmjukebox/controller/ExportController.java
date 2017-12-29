package uk.co.mpcontracting.rpmjukebox.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableCell;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableModel;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;

@Slf4j
@FXMLController
public class ExportController implements Constants {

    @FXML
    private TableView<PlaylistTableModel> playlistTableView;

    @FXML
    private TableColumn<PlaylistTableModel, Boolean> selectColumn;

    @FXML
    private TableColumn<PlaylistTableModel, String> playlistColumn;

    @FXML
    private Button cancelButton;

    @Autowired
    private ExportView exportView;

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private PlaylistManager playlistManager;

    @Value("${playlist.file.extension}")
    private String playlistFileExtension;

    private ObservableList<PlaylistTableModel> observablePlaylists;
    private Set<Integer> playlistsToExport;
    private String playlistExtensionFilter;

    @FXML
    public void initialize() {
        log.info("Initialising ExportController");

        observablePlaylists = FXCollections.observableArrayList();
        playlistTableView.setPlaceholder(new Label(""));
        playlistTableView.setItems(observablePlaylists);
        playlistTableView.setEditable(true);
        playlistTableView.setSelectionModel(null);

        // Hide the table header
        playlistTableView.widthProperty().addListener((observable, oldValue, newValue) -> {
            Pane header = (Pane)playlistTableView.lookup("TableHeaderRow");
            header.setMaxHeight(0);
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setVisible(false);
            header.setManaged(false);
        });

        // Cell factories
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        playlistColumn.setCellFactory(tableColumn -> {
            return new PlaylistTableCell<>();
        });

        // Cell value factories
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().getSelected());
        playlistColumn.setCellValueFactory(cellData -> cellData.getValue().getName());

        // Set the select column to be editable
        selectColumn.setEditable(true);

        playlistsToExport = new HashSet<>();
        playlistExtensionFilter = "*." + playlistFileExtension;
    }

    public void bindPlaylists() {
        observablePlaylists.clear();
        playlistsToExport.clear();

        List<Playlist> playlists = playlistManager.getPlaylists();

        playlists.forEach(playlist -> {
            if (playlist.getPlaylistId() > 0 || playlist.getPlaylistId() == PLAYLIST_ID_FAVOURITES) {
                PlaylistTableModel tableModel = new PlaylistTableModel(playlist);

                tableModel.getSelected().addListener((observable, oldValue, newValue) -> {
                    setPlaylistToExport(tableModel.getPlaylist().getPlaylistId(), newValue);
                });

                observablePlaylists.add(tableModel);
            }
        });

        cancelButton.requestFocus();
    }

    private void setPlaylistToExport(int playlistId, boolean export) {
        log.debug("Setting playlist to export : ID - " + playlistId + ", Export - " + export);

        if (export) {
            playlistsToExport.add(playlistId);
        } else {
            playlistsToExport.remove(playlistId);
        }
    }

    @FXML
    protected void handleOkButtonAction(ActionEvent event) {
        log.debug("Exporting playlists - " + playlistsToExport);

        if (!playlistsToExport.isEmpty()) {
            FileChooser fileChooser = constructFileChooser();
            fileChooser.setTitle(messageManager.getMessage(MESSAGE_EXPORT_PLAYLIST_TITLE));
            fileChooser.getExtensionFilters()
                .add(new ExtensionFilter(
                    messageManager.getMessage(MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER, playlistExtensionFilter),
                    playlistExtensionFilter));

            File file = fileChooser.showSaveDialog(RpmJukebox.getStage());

            if (file != null) {
                List<PlaylistSettings> playlists = new ArrayList<>();

                playlistsToExport.forEach(
                    playlistId -> playlists.add(new PlaylistSettings(playlistManager.getPlaylist(playlistId))));

                try (FileWriter fileWriter = constructFileWriter(file)) {
                    fileWriter.write(settingsManager.getGson().toJson(playlists));
                } catch (Exception e) {
                    log.error("Unable to export playlists file - " + file.getAbsolutePath(), e);
                }

                exportView.close();
            }
        } else {
            exportView.close();
        }
    }

    @FXML
    protected void handleCancelButtonAction(ActionEvent event) {
        exportView.close();
    }

    // Package level for testing purposes
    FileChooser constructFileChooser() {
        return new FileChooser();
    }

    // Package level for testing purposes
    FileWriter constructFileWriter(File file) throws Exception {
        return new FileWriter(file);
    }
}
