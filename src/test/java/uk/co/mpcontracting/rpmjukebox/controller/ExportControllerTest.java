package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getTestResourceContent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableModel;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;

class ExportControllerTest extends AbstractGuiTest {

  @MockBean
  private SettingsService settingsService;

  @MockBean
  private PlaylistService playlistService;

  @SpyBean
  private ExportView exportView;

  @SpyBean
  private ExportController underTest;

  private Button cancelButton;

  @SneakyThrows
  @PostConstruct
  public void postConstruct() {
    init(exportView);
  }

  @BeforeEach
  void beforeEach() {
    cancelButton = spy(find("#cancelButton"));
    setField(underTest, "cancelButton", cancelButton);
  }

  @Test
  @SneakyThrows
  void shouldBindPlaylists() {
    when(playlistService.getPlaylists()).thenReturn(createPlaylists());

    Platform.runLater(() -> underTest.bindPlaylists());

    WaitForAsyncUtils.waitForFxEvents();

    @SuppressWarnings("unchecked")
    ObservableList<PlaylistTableModel> observablePlaylists = getField(underTest, "observablePlaylists", ObservableList.class);

    assertThat(observablePlaylists).hasSize(8);
    verify(cancelButton).requestFocus();
  }

  @Test
  @SneakyThrows
  void shouldAddPlaylistToExport() {
    when(playlistService.getPlaylists()).thenReturn(createPlaylists());

    Platform.runLater(() -> underTest.bindPlaylists());

    WaitForAsyncUtils.waitForFxEvents();

    PlaylistTableModel playlistTableModel1 = (PlaylistTableModel) getNonNullField(underTest, "observablePlaylists", ObservableList.class).getFirst();
    PlaylistTableModel playlistTableModel2 = (PlaylistTableModel) getNonNullField(underTest, "observablePlaylists", ObservableList.class).get(1);

    playlistTableModel1.getSelected().set(true);
    playlistTableModel2.getSelected().set(true);

    @SuppressWarnings("unchecked")
    Set<Integer> playlistsToExport = (Set<Integer>) getField(underTest, "playlistsToExport", Set.class);

    assertThat(playlistsToExport).hasSize(2);
    assertThat(requireNonNull(playlistsToExport).toString()).isEqualTo("[-2, 1]");
  }

  @Test
  @SneakyThrows
  void shouldRemovePlaylistToExport() {
    when(playlistService.getPlaylists()).thenReturn(createPlaylists());

    Platform.runLater(() -> underTest.bindPlaylists());

    WaitForAsyncUtils.waitForFxEvents();

    PlaylistTableModel playlistTableModel1 = (PlaylistTableModel) getNonNullField(underTest, "observablePlaylists", ObservableList.class).getFirst();
    PlaylistTableModel playlistTableModel2 = (PlaylistTableModel) getNonNullField(underTest, "observablePlaylists", ObservableList.class).get(1);

    playlistTableModel1.getSelected().set(true);
    playlistTableModel2.getSelected().set(true);
    playlistTableModel1.getSelected().set(false);

    @SuppressWarnings("unchecked")
    Set<Integer> playlistsToExport = (Set<Integer>) getField(underTest, "playlistsToExport", Set.class);

    assertThat(playlistsToExport).hasSize(1);
    assertThat(requireNonNull(playlistsToExport).toString()).isEqualTo("[1]");
  }

  @Test
  @SneakyThrows
  void shouldClickOkButton() {
    doNothing().when(exportView).close();

    List<Playlist> playlists = createPlaylists();
    when(playlistService.getPlaylist(1)).thenReturn(of(playlists.get(3)));
    when(playlistService.getPlaylist(2)).thenReturn(of(playlists.get(4)));

    Set<Integer> playlistsToExport = new HashSet<>(List.of(1, 2));
    setField(underTest, "playlistsToExport", playlistsToExport);

    FileChooser mockFileChooser = mock(FileChooser.class);
    when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File mockFile = mock(File.class);
    when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
    doReturn(mockFileChooser).when(underTest).constructFileChooser();

    FileWriter mockFileWriter = mock(FileWriter.class);
    doReturn(mockFileWriter).when(underTest).constructFileWriter(mockFile);

    when(settingsService.getGson()).thenReturn(new Gson());

    underTest.handleOkButtonAction();

    ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockFileWriter, atLeastOnce()).write(jsonCaptor.capture());

    String testJson = getTestResourceContent("json/exportController-shouldClickOkButton.json").trim();
    String result = jsonCaptor.getValue().trim();

    assertThat(result).isEqualTo(testJson);
    verify(exportView).close();
  }

  @Test
  @SneakyThrows
  void shouldClickOkButtonWithExceptionThrownFromFileWriter() {
    doNothing().when(exportView).close();

    List<Playlist> playlists = createPlaylists();
    when(playlistService.getPlaylist(1)).thenReturn(of(playlists.get(3)));
    when(playlistService.getPlaylist(2)).thenReturn(of(playlists.get(4)));

    Set<Integer> playlistsToExport = new HashSet<>(List.of(1, 2));
    setField(underTest, "playlistsToExport", playlistsToExport);

    FileChooser mockFileChooser = mock(FileChooser.class);
    when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File mockFile = mock(File.class);
    when(mockFileChooser.showSaveDialog(any())).thenReturn(mockFile);
    doReturn(mockFileChooser).when(underTest).constructFileChooser();

    FileWriter mockFileWriter = mock(FileWriter.class);
    doReturn(mockFileWriter).when(underTest).constructFileWriter(mockFile);
    doThrow(new RuntimeException("ExportControllerTest.shouldClickOkButtonWithExceptionThrownFromFileWriter()"))
        .when(mockFileWriter).write(anyString());

    when(settingsService.getGson()).thenReturn(new Gson());

    underTest.handleOkButtonAction();

    verify(mockFileWriter).write(anyString());
    verify(exportView).close();
  }

  @Test
  void shouldClickOkButtonWithNullFileReturnedFromFileChooser() {
    Set<Integer> playlistsToExport = new HashSet<>(List.of(1, 2));
    setField(underTest, "playlistsToExport", playlistsToExport);

    FileChooser mockFileChooser = mock(FileChooser.class);
    when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
    when(mockFileChooser.showSaveDialog(any())).thenReturn(null);
    doReturn(mockFileChooser).when(underTest).constructFileChooser();

    underTest.handleOkButtonAction();

    verify(exportView, never()).close();
  }

  @Test
  void shouldClickOkButtonWithEmptyPlaylistsToExport() {
    doNothing().when(exportView).close();

    setField(underTest, "playlistsToExport", new HashSet<>());

    underTest.handleOkButtonAction();

    verify(exportView).close();
  }

  @Test
  void shouldClickCancelButton() {
    doNothing().when(exportView).close();

    underTest.handleCancelButtonAction();

    verify(exportView).close();
  }

  private List<Playlist> createPlaylists() {
    List<Playlist> playlists = new ArrayList<>();

    for (int i = -2; i < 8; i++) {
      playlists.add(new Playlist(i, "Playlist " + i, 10));
    }

    return playlists;
  }
}