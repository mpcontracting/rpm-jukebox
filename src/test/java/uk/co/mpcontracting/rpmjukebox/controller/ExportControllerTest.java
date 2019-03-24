package uk.co.mpcontracting.rpmjukebox.controller;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableModel;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getTestResourceContent;

public class ExportControllerTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private ExportController exportController;

    @Autowired
    private ExportView exportView;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private PlaylistManager mockPlaylistManager;

    private ExportController spyExportController;
    private ExportView spyExportView;
    private Button spyCancelButton;

    @PostConstruct
    public void constructView() throws Exception {
        init(exportView);
    }

    @Before
    public void setup() {
        spyExportController = spy(exportController);
        spyExportView = spy(exportView);

        setField(spyExportController, "settingsManager", mockSettingsManager);
        setField(spyExportController, "playlistManager", mockPlaylistManager);
        setField(spyExportController, "exportView", spyExportView);

        doNothing().when(spyExportView).close();

        spyCancelButton = spy(find("#cancelButton"));
        setField(spyExportController, "cancelButton", spyCancelButton);
    }

    @Test
    public void shouldBindPlaylists() throws Exception {
        when(mockPlaylistManager.getPlaylists()).thenReturn(generatePlaylists());

        CountDownLatch latch = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            spyExportController.bindPlaylists();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        ObservableList<PlaylistTableModel> observablePlaylists = (ObservableList<PlaylistTableModel>) getField(spyExportController, "observablePlaylists");

        assertThat(observablePlaylists).hasSize(8);
        verify(spyCancelButton, times(1)).requestFocus();
    }

    @Test
    public void shouldAddPlaylistToExport() throws Exception {
        when(mockPlaylistManager.getPlaylists()).thenReturn(generatePlaylists());

        CountDownLatch latch = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            spyExportController.bindPlaylists();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel1 = ((ObservableList<PlaylistTableModel>) getField(spyExportController, "observablePlaylists")).get(0);
        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel2 = ((ObservableList<PlaylistTableModel>) getField(spyExportController, "observablePlaylists")).get(1);

        playlistTableModel1.getSelected().set(true);
        playlistTableModel2.getSelected().set(true);

        @SuppressWarnings("unchecked")
        Set<Integer> playlistsToExport = (Set<Integer>) getField(spyExportController,
            "playlistsToExport");

        assertThat(playlistsToExport).hasSize(2);
        assertThat(playlistsToExport.toString()).isEqualTo("[-2, 1]");
    }

    @Test
    public void shouldRemovePlaylistToExport() throws Exception {
        when(mockPlaylistManager.getPlaylists()).thenReturn(generatePlaylists());

        CountDownLatch latch = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            spyExportController.bindPlaylists();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel1 = ((ObservableList<PlaylistTableModel>) getField(spyExportController, "observablePlaylists")).get(0);
        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel2 = ((ObservableList<PlaylistTableModel>) getField(spyExportController, "observablePlaylists")).get(1);

        playlistTableModel1.getSelected().set(true);
        playlistTableModel2.getSelected().set(true);
        playlistTableModel1.getSelected().set(false);

        @SuppressWarnings("unchecked")
        Set<Integer> playlistsToExport = (Set<Integer>) getField(spyExportController,
            "playlistsToExport");

        assertThat(playlistsToExport).hasSize(1);
        assertThat(playlistsToExport.toString()).isEqualTo("[1]");
    }

    @Test
    public void shouldClickOkButton() throws Exception {
        List<Playlist> playlists = generatePlaylists();
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(of(playlists.get(3)));
        when(mockPlaylistManager.getPlaylist(2)).thenReturn(of(playlists.get(4)));

        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(1, 2));
        setField(spyExportController, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyExportController).constructFileChooser();

        FileWriter mockFileWriter = mock(FileWriter.class);
        doReturn(mockFileWriter).when(spyExportController).constructFileWriter(any());

        when(mockSettingsManager.getGson()).thenReturn(new Gson());

        spyExportController.handleOkButtonAction(new ActionEvent());

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockFileWriter, atLeastOnce()).write(jsonCaptor.capture());

        String testJson = getTestResourceContent("json/exportController-shouldClickOkButton.json").trim();
        String result = jsonCaptor.getValue().trim();

        assertThat(result).isEqualTo(testJson);
        verify(spyExportView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWithExceptionThrownFromFileWriter() throws Exception {
        List<Playlist> playlists = generatePlaylists();
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(of(playlists.get(3)));
        when(mockPlaylistManager.getPlaylist(2)).thenReturn(of(playlists.get(4)));

        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(1, 2));
        setField(spyExportController, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyExportController).constructFileChooser();

        FileWriter mockFileWriter = mock(FileWriter.class);
        doThrow(new RuntimeException("ExportControllerTest.shouldClickOkButtonWithExceptionThrownFromFileWriter()"))
            .when(mockFileWriter).write(anyString());
        doReturn(mockFileWriter).when(spyExportController).constructFileWriter(any());

        spyExportController.handleOkButtonAction(new ActionEvent());

        verify(mockFileWriter, never()).write(anyString());
        verify(spyExportView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWithNullFileReturnedFromFileChooser() {
        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(new Integer[] { 1, 2 }));
        setField(spyExportController, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
        when(mockFileChooser.showSaveDialog(any())).thenReturn(null);
        doReturn(mockFileChooser).when(spyExportController).constructFileChooser();

        spyExportController.handleOkButtonAction(new ActionEvent());

        verify(spyExportView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWithEmptyPlaylistsToExport() {
        setField(spyExportController, "playlistsToExport", new HashSet<>());

        spyExportController.handleOkButtonAction(new ActionEvent());

        verify(spyExportView, times(1)).close();
    }

    @Test
    public void shouldClickCancelButton() {
        spyExportController.handleCancelButtonAction(new ActionEvent());

        verify(spyExportView, times(1)).close();
    }

    private List<Playlist> generatePlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        for (int i = -2; i < 8; i++) {
            playlists.add(new Playlist(i, "Playlist " + i, 10));
        }

        return playlists;
    }
}
