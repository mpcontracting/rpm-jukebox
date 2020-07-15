package uk.co.mpcontracting.rpmjukebox.controller;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.util.WaitForAsyncUtils;
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

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getTestResourceContent;

public class ExportControllerTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private ExportController exportController;

    @Autowired
    private ExportView originalExportView;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private PlaylistManager playlistManager;

    private ExportController underTest;
    private ExportView exportView;
    private Button cancelButton;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(originalExportView);
    }

    @Before
    public void setup() {
        underTest = spy(exportController);
        exportView = spy(originalExportView);

        setField(underTest, "settingsManager", settingsManager);
        setField(underTest, "playlistManager", playlistManager);
        setField(underTest, "exportView", exportView);

        doNothing().when(exportView).close();

        cancelButton = spy(find("#cancelButton"));
        setField(underTest, "cancelButton", cancelButton);
    }

    @Test
    @SneakyThrows
    public void shouldBindPlaylists() {
        when(playlistManager.getPlaylists()).thenReturn(generatePlaylists());

        threadRunner.runOnGui(() -> underTest.bindPlaylists());

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        ObservableList<PlaylistTableModel> observablePlaylists = (ObservableList<PlaylistTableModel>) getField(underTest, "observablePlaylists");

        assertThat(observablePlaylists).hasSize(8);
        verify(cancelButton, times(1)).requestFocus();
    }

    @Test
    @SneakyThrows
    public void shouldAddPlaylistToExport() {
        when(playlistManager.getPlaylists()).thenReturn(generatePlaylists());

        threadRunner.runOnGui(() -> underTest.bindPlaylists());

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel1 = ((ObservableList<PlaylistTableModel>) getNonNullField(underTest, "observablePlaylists")).get(0);
        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel2 = ((ObservableList<PlaylistTableModel>) getNonNullField(underTest, "observablePlaylists")).get(1);

        playlistTableModel1.getSelected().set(true);
        playlistTableModel2.getSelected().set(true);

        @SuppressWarnings("unchecked")
        Set<Integer> playlistsToExport = (Set<Integer>) getField(underTest, "playlistsToExport");

        assertThat(playlistsToExport).hasSize(2);
        assertThat(requireNonNull(playlistsToExport).toString()).isEqualTo("[-2, 1]");
    }

    @Test
    @SneakyThrows
    public void shouldRemovePlaylistToExport() {
        when(playlistManager.getPlaylists()).thenReturn(generatePlaylists());

        threadRunner.runOnGui(() -> underTest.bindPlaylists());

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel1 = ((ObservableList<PlaylistTableModel>) getNonNullField(underTest, "observablePlaylists")).get(0);
        @SuppressWarnings("unchecked")
        PlaylistTableModel playlistTableModel2 = ((ObservableList<PlaylistTableModel>) getNonNullField(underTest, "observablePlaylists")).get(1);

        playlistTableModel1.getSelected().set(true);
        playlistTableModel2.getSelected().set(true);
        playlistTableModel1.getSelected().set(false);

        @SuppressWarnings("unchecked")
        Set<Integer> playlistsToExport = (Set<Integer>) getField(underTest, "playlistsToExport");

        assertThat(playlistsToExport).hasSize(1);
        assertThat(requireNonNull(playlistsToExport).toString()).isEqualTo("[1]");
    }

    @Test
    @SneakyThrows
    public void shouldClickOkButton() {
        List<Playlist> playlists = generatePlaylists();
        when(playlistManager.getPlaylist(1)).thenReturn(of(playlists.get(3)));
        when(playlistManager.getPlaylist(2)).thenReturn(of(playlists.get(4)));

        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(1, 2));
        setField(underTest, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(underTest).constructFileChooser();

        FileWriter mockFileWriter = mock(FileWriter.class);
        doReturn(mockFileWriter).when(underTest).constructFileWriter(any());

        when(settingsManager.getGson()).thenReturn(new Gson());

        underTest.handleOkButtonAction();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockFileWriter, atLeastOnce()).write(jsonCaptor.capture());

        String testJson = getTestResourceContent("json/exportController-shouldClickOkButton.json").trim();
        String result = jsonCaptor.getValue().trim();

        assertThat(result).isEqualTo(testJson);
        verify(exportView, times(1)).close();
    }

    @Test
    @SneakyThrows
    public void shouldClickOkButtonWithExceptionThrownFromFileWriter() {
        List<Playlist> playlists = generatePlaylists();
        when(playlistManager.getPlaylist(1)).thenReturn(of(playlists.get(3)));
        when(playlistManager.getPlaylist(2)).thenReturn(of(playlists.get(4)));

        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(1, 2));
        setField(underTest, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(underTest).constructFileChooser();

        FileWriter mockFileWriter = mock(FileWriter.class);
        doThrow(new RuntimeException("ExportControllerTest.shouldClickOkButtonWithExceptionThrownFromFileWriter()"))
                .when(mockFileWriter).write(anyString());
        doReturn(mockFileWriter).when(underTest).constructFileWriter(any());

        underTest.handleOkButtonAction();

        verify(mockFileWriter, never()).write(anyString());
        verify(exportView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWithNullFileReturnedFromFileChooser() {
        Set<Integer> playlistsToExport = new HashSet<>(Arrays.asList(1, 2));
        setField(underTest, "playlistsToExport", playlistsToExport);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
        when(mockFileChooser.showSaveDialog(any())).thenReturn(null);
        doReturn(mockFileChooser).when(underTest).constructFileChooser();

        underTest.handleOkButtonAction();

        verify(exportView, never()).close();
    }

    @Test
    public void shouldClickOkButtonWithEmptyPlaylistsToExport() {
        setField(underTest, "playlistsToExport", new HashSet<>());

        underTest.handleOkButtonAction();

        verify(exportView, times(1)).close();
    }

    @Test
    public void shouldClickCancelButton() {
        underTest.handleCancelButtonAction();

        verify(exportView, times(1)).close();
    }

    private List<Playlist> generatePlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        for (int i = -2; i < 8; i++) {
            playlists.add(new Playlist(i, "Playlist " + i, 10));
        }

        return playlists;
    }
}
