package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.BACK_SPACE;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.BUFFER_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.DATA_INDEXED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.END_OF_MEDIA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PAUSED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_STOPPED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_NEXT;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_PLAY_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_PREVIOUS;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_REPEAT;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_SHUFFLE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_DOWN;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_MUTE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_UP;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_ADD_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_DELETE_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_RANDOM_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_EXPORT_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_IMPORT_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_SETTINGS;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_VIEW_EQUALIZER;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MUTE_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.NEW_VERSION_AVAILABLE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CREATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_DELETED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TIME_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_QUEUED_FOR_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ALL;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.OFF;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ONE;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createKeyEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createVersion;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createYearString;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_PLAY;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_ALL;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_ONE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_SHUFFLE_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_SHUFFLE_ON;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_VOLUME_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_VOLUME_ON;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_NEW_VERSION_AVAILABLE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import com.google.gson.Gson;
import com.igormaznitsa.commons.version.Version;
import de.felixroske.jfxsupport.GUIState;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.component.ImageFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.service.MediaService;
import uk.co.mpcontracting.rpmjukebox.service.NativeService;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SearchService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.service.UpdateService;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;
import uk.co.mpcontracting.rpmjukebox.view.MessageView;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

class MainPanelControllerTest extends AbstractGuiTest {

  @Autowired
  private ApplicationProperties applicationProperties;

  @MockBean
  private ImageFactory imageFactory;

  @MockBean
  private ConfirmView confirmView;

  @MockBean
  private EqualizerView equalizerView;

  @MockBean
  private ExportView exportView;

  @MockBean
  private MessageView messageView;

  @MockBean
  private SettingsView settingsView;

  @MockBean
  private TrackTableView trackTableView;

  @MockBean
  private EqualizerController equalizerController;

  @MockBean
  private ExportController exportController;

  @MockBean
  private SettingsController settingsController;

  @MockBean
  private TrackTableController trackTableController;

  @MockBean
  private CacheService cacheService;

  @MockBean
  private MediaService mediaService;

  @MockBean
  private NativeService nativeService;

  @MockBean
  private PlaylistService playlistService;

  @MockBean
  private SearchService searchService;

  @MockBean
  private SettingsService settingsService;

  @Autowired
  private StringResourceService stringResourceService;

  @MockBean
  private UpdateService updateService;

  @Autowired
  private MainPanelView mainPanelView;

  @SpyBean
  private MainPanelController underTest;

  private Stage existingStage;
  private Parent root;

  @SneakyThrows
  @PostConstruct
  void postConstruct() {
    init(mainPanelView);
  }

  @BeforeEach
  void beforeEach() {
    existingStage = GUIState.getStage();

    Stage stage = mock(Stage.class);
    Scene scene = mock(Scene.class);
    root = mock(Parent.class);
    when(stage.getScene()).thenReturn(scene);
    when(scene.getRoot()).thenReturn(root);
    setField(GUIState.class, "stage", stage);

    Platform.runLater(() -> {
      getNonNullField(underTest, "searchTextField", TextField.class).setText(null);
      getNonNullField(underTest, "yearFilterComboBox", ComboBox.class).getItems().clear();
      getNonNullField(underTest, "playlistPanelListView", ListView.class).getItems().clear();
    });

    WaitForAsyncUtils.waitForFxEvents();

    reset(eventProcessor);
    reset(playlistService);
    reset(searchService);
  }

  @AfterEach
  void afterEach() {
    setField(GUIState.class, "stage", existingStage);
  }

  /////////////////////////
  // Component Listeners //
  /////////////////////////

  @Test
  void shouldUpdateSearchTextSearchCriteria() {
    String searchText = getFaker().lorem().characters(10, 20);
    TextField searchTextField = getNonNullField(underTest, "searchTextField", TextField.class);

    Platform.runLater(() -> searchTextField.setText(searchText));

    WaitForAsyncUtils.waitForFxEvents();

    verify(searchService).search(new TrackSearch(searchText));
    verify(eventProcessor).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldUpdateYearFilterSearchCriteria() {
    String year1 = createYearString();
    String year2 = createYearString();
    String year3 = createYearString();

    @SuppressWarnings("unchecked")
    ComboBox<YearFilter> yearFilterComboBox = getNonNullField(underTest, "yearFilterComboBox", ComboBox.class);
    yearFilterComboBox.getItems().add(new YearFilter(year1, year1));
    yearFilterComboBox.getItems().add(new YearFilter(year2, year2));
    yearFilterComboBox.getItems().add(new YearFilter(year3, year3));

    Platform.runLater(() -> yearFilterComboBox.getSelectionModel().select(1));

    WaitForAsyncUtils.waitForFxEvents();

    verify(playlistService).setPlaylistTracks(PLAYLIST_ID_SEARCH, emptyList());
    verify(eventProcessor, never()).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldUpdateYearFilterAndSearchTextSearchCriteria() {
    String year1 = createYearString();
    String year2 = createYearString();
    String year3 = createYearString();

    @SuppressWarnings("unchecked")
    ComboBox<YearFilter> yearFilterComboBox = getNonNullField(underTest, "yearFilterComboBox", ComboBox.class);
    yearFilterComboBox.getItems().add(new YearFilter(year1, year1));
    yearFilterComboBox.getItems().add(new YearFilter(year2, year2));
    yearFilterComboBox.getItems().add(new YearFilter(year3, year3));

    String searchText = getFaker().lorem().characters(10, 20);
    TextField searchTextField = getNonNullField(underTest, "searchTextField", TextField.class);

    Platform.runLater(() -> {
      yearFilterComboBox.getSelectionModel().select(1);
      searchTextField.setText(searchText);
    });

    WaitForAsyncUtils.waitForFxEvents();

    verify(searchService).search(new TrackSearch(searchText, new TrackFilter(null, year2)));
    verify(eventProcessor).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldUpdatePlayingPlaylistOnYearFilterUpdate() {
    String year1 = createYearString();
    String year2 = createYearString();
    String year3 = createYearString();

    @SuppressWarnings("unchecked")
    ComboBox<YearFilter> yearFilterComboBox = getNonNullField(underTest, "yearFilterComboBox", ComboBox.class);
    yearFilterComboBox.getItems().add(new YearFilter(year1, year1));
    yearFilterComboBox.getItems().add(new YearFilter(year2, year2));
    yearFilterComboBox.getItems().add(new YearFilter(year3, year3));

    @SuppressWarnings("unchecked")
    List<Track> tracks = mock(List.class);

    Playlist playlist = mock(Playlist.class);
    when(playlist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);
    when(playlist.getTracks()).thenReturn(tracks);
    when(playlistService.getPlayingPlaylist()).thenReturn(playlist);

    Platform.runLater(() -> yearFilterComboBox.getSelectionModel().select(1));

    WaitForAsyncUtils.waitForFxEvents();

    verify(playlistService).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
    verify(playlist).getTracks();
    verify(eventProcessor, never()).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
  }

  //////////////////////
  // Manipulate Views //
  //////////////////////

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldShowMessageView(boolean blurBackground) {
    String message = getFaker().lorem().characters(10, 20);

    underTest.showMessageView(message, blurBackground);

    verify(messageView).setMessage(message);
    verify(messageView).show(blurBackground);
  }

  @Test
  void shouldShowMessageViewAlreadyShowing() {
    String message = getFaker().lorem().characters(10, 20);

    when(messageView.isShowing()).thenReturn(true);

    underTest.showMessageView(message, true);

    verify(messageView).setMessage(message);
    verify(messageView, never()).show(anyBoolean());
  }

  @Test
  void shouldCloseMessageView() {
    underTest.closeMessageView();

    verify(messageView).close();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldShowConfirmView(boolean blurBackground) {
    String message = getFaker().lorem().characters(10, 20);
    Runnable okRunnable = mock(Runnable.class);
    Runnable cancelRunnable = mock(Runnable.class);

    underTest.showConfirmView(message, blurBackground, okRunnable, cancelRunnable);

    verify(confirmView).setMessage(message);
    verify(confirmView).setRunnables(okRunnable, cancelRunnable);
    verify(confirmView).show(blurBackground);
  }

  @Test
  void shouldShowConfirmViewAlreadyShowing() {
    String message = getFaker().lorem().characters(10, 20);
    Runnable okRunnable = mock(Runnable.class);
    Runnable cancelRunnable = mock(Runnable.class);

    when(confirmView.isShowing()).thenReturn(true);

    underTest.showConfirmView(message, true, okRunnable, cancelRunnable);

    verify(confirmView).setMessage(message);
    verify(confirmView).setRunnables(okRunnable, cancelRunnable);
    verify(confirmView, never()).show(anyBoolean());
  }

  //////////////////////
  // GUI Interactions //
  //////////////////////

  @Test
  void shouldClickNewVersionButton() {
    clickOnNode("#newVersionButton");

    verify(updateService).downloadNewVersion();
  }

  @Test
  void shouldClickAddPlaylistButton() {
    clickOnNode("#addPlaylistButton");

    verify(playlistService).createPlaylist();
  }

  @Test
  void shouldClickDeletePlaylistButton() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Playlist playlist = new Playlist(1, createPlaylistName(), 10);

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(playlist);
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    clickOnNode("#deletePlaylistButton");

    ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

    verify(confirmView).setMessage(stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
    verify(confirmView).setRunnables(okRunnable.capture(), any());
    verify(confirmView).show(anyBoolean());

    okRunnable.getValue().run();

    verify(playlistService).deletePlaylist(playlist.getPlaylistId());
  }

  @Test
  void shouldClickDeletePlaylistButtonWithReservedPlaylist() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Playlist playlist = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(playlist);
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    clickOnNode("#deletePlaylistButton");

    verify(confirmView, never()).show(anyBoolean());
  }

  @Test
  void shouldClickDeletePlaylistButtonWithNullPlaylist() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> playlistPanelListView.getSelectionModel().clearSelection());

    WaitForAsyncUtils.waitForFxEvents();

    clickOnNode("#deletePlaylistButton");

    verify(confirmView, never()).show(anyBoolean());
  }

  @Test
  void shouldClickSettingsButton() {
    clickOnNode("#settingsButton");

    verify(settingsController).bindSystemSettings();
    verify(settingsView).show(true);
  }

  @Test
  @SneakyThrows
  void shouldClickImportPlaylistButton() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    FileChooser fileChooser = mock(FileChooser.class);
    when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File file = mock(File.class);
    when(fileChooser.showOpenDialog(any())).thenReturn(file);
    doReturn(fileChooser).when(underTest).constructFileChooser();

    FileReader fileReader = mock(FileReader.class);
    doReturn(fileReader).when(underTest).constructFileReader(any());

    Gson gson = mock(Gson.class);
    when(settingsService.getGson()).thenReturn(gson);

    Playlist playlist = new Playlist(1, createPlaylistName(), 10);
    for (int i = 0; i < 10; i++) {
      Track track = mock(Track.class);
      when(track.getTrackId()).thenReturn(Integer.toString(i));

      playlist.addTrack(track);
      when(searchService.getTrackById(Integer.toString(i))).thenReturn(of(track));
    }

    List<PlaylistSettings> playlistSettings = new ArrayList<>();
    playlistSettings.add(new PlaylistSettings(playlist));

    when(gson.fromJson(any(FileReader.class), any(Type.class))).thenReturn(playlistSettings);

    Platform.runLater(underTest::handleImportPlaylistButtonAction);

    WaitForAsyncUtils.waitForFxEvents();

    ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

    verify(playlistService).addPlaylist(playlistCaptor.capture());

    Playlist result = playlistCaptor.getValue();

    assertThat(result).isEqualTo(playlist);
    assertThat(result.getTracks()).hasSize(playlist.getTracks().size());

    verify(playlistService).getPlaylists();
    verify(root).setEffect(any(BoxBlur.class));
    verify(root).setEffect(null);
  }

  @Test
  @SneakyThrows
  void shouldClickImportPlaylistButtonWithNullTracksFromSearch() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    FileChooser fileChooser = mock(FileChooser.class);
    when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File file = mock(File.class);
    when(fileChooser.showOpenDialog(any())).thenReturn(file);
    doReturn(fileChooser).when(underTest).constructFileChooser();

    FileReader fileReader = mock(FileReader.class);
    doReturn(fileReader).when(underTest).constructFileReader(any());

    Gson gson = mock(Gson.class);
    when(settingsService.getGson()).thenReturn(gson);

    Playlist playlist = new Playlist(1, createPlaylistName(), 10);
    for (int i = 0; i < 10; i++) {
      Track track = mock(Track.class);
      when(track.getTrackId()).thenReturn(Integer.toString(i));

      playlist.addTrack(track);
    }

    List<PlaylistSettings> playlistSettings = new ArrayList<>();
    playlistSettings.add(new PlaylistSettings(playlist));

    when(gson.fromJson(any(FileReader.class), any(Type.class))).thenReturn(playlistSettings);
    when(searchService.getTrackById(any())).thenReturn(empty());

    Platform.runLater(underTest::handleImportPlaylistButtonAction);

    WaitForAsyncUtils.waitForFxEvents();

    ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

    verify(playlistService).addPlaylist(playlistCaptor.capture());

    Playlist result = playlistCaptor.getValue();

    assertThat(result).isEqualTo(playlist);
    assertThat(result.getTracks()).isEmpty();

    verify(playlistService).getPlaylists();
    verify(root).setEffect(any(BoxBlur.class));
    verify(root).setEffect(null);
  }

  @Test
  @SneakyThrows
  void shouldClickImportPlaylistButtonWithNullPlaylistSettings() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    FileChooser fileChooser = mock(FileChooser.class);
    when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File file = mock(File.class);
    when(fileChooser.showOpenDialog(any())).thenReturn(file);
    doReturn(fileChooser).when(underTest).constructFileChooser();

    FileReader fileReader = mock(FileReader.class);
    doReturn(fileReader).when(underTest).constructFileReader(any());

    Gson gson = mock(Gson.class);
    when(settingsService.getGson()).thenReturn(gson);
    when(gson.fromJson(any(FileReader.class), any(Type.class))).thenReturn(null);

    Platform.runLater(underTest::handleImportPlaylistButtonAction);

    WaitForAsyncUtils.waitForFxEvents();

    verify(playlistService, never()).addPlaylist(any());
    verify(playlistService, never()).getPlaylists();
    verify(root).setEffect(any(BoxBlur.class));
    verify(root).setEffect(null);
  }

  @Test
  void shouldClickImportPlaylistButtonWithNullFile() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    FileChooser fileChooser = mock(FileChooser.class);
    when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
    when(fileChooser.showOpenDialog(any())).thenReturn(null);
    doReturn(fileChooser).when(underTest).constructFileChooser();

    Platform.runLater(underTest::handleImportPlaylistButtonAction);

    WaitForAsyncUtils.waitForFxEvents();

    verify(playlistService, never()).addPlaylist(any());
    verify(playlistService, never()).getPlaylists();
    verify(root).setEffect(any(BoxBlur.class));
    verify(root).setEffect(null);
  }

  @Test
  @SneakyThrows
  void shouldClickImportPlaylistButtonWhenExceptionThrown() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    FileChooser fileChooser = mock(FileChooser.class);
    when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

    File file = mock(File.class);
    when(fileChooser.showOpenDialog(any())).thenReturn(file);
    doReturn(fileChooser).when(underTest).constructFileChooser();

    doThrow(new RuntimeException("MainPanelControllerTest.shouldClickImportPlaylistButtonWhenExceptionThrown()"))
        .when(underTest).constructFileReader(any());

    Platform.runLater(underTest::handleImportPlaylistButtonAction);

    WaitForAsyncUtils.waitForFxEvents();

    verify(playlistService, never()).addPlaylist(any());
    verify(playlistService, never()).getPlaylists();
    verify(root).setEffect(any(BoxBlur.class));
    verify(root).setEffect(null);
  }

  @Test
  void shouldClickExportPlaylistButton() {
    clickOnNode("#exportPlaylistButton");

    verify(exportController).bindPlaylists();
    verify(exportView).show(true);
  }

  @Test
  void shouldClickPreviousButton() {
    clickOnNode("#previousButton");

    verify(mediaService, never()).setSeekPositionPercent(0d);
    verify(playlistService).playPreviousTrack(true);
  }

  @Test
  void shouldClickPreviousButtonWhenPlayingLessThanEqualCutoff() {
    when(mediaService.getPlayingTimeSeconds()).thenReturn((double) applicationProperties.getPreviousSecondsCutoff());

    clickOnNode("#previousButton");

    verify(mediaService, never()).setSeekPositionPercent(0d);
    verify(playlistService).playPreviousTrack(true);
  }

  @Test
  void shouldClickPreviousButtonWhenPlayingGreaterThanCutoff() {
    when(mediaService.getPlayingTimeSeconds()).thenReturn(applicationProperties.getPreviousSecondsCutoff() + 1d);

    clickOnNode("#previousButton");

    verify(mediaService).setSeekPositionPercent(0d);
    verify(playlistService, never()).playPreviousTrack(true);
  }

  @Test
  void shouldClickPlayPauseButton() {
    Playlist playlist = mock(Playlist.class);
    when(playlist.isEmpty()).thenReturn(true);
    when(playlistService.getPlaylist(anyInt())).thenReturn(of(playlist));

    clickOnNode("#playPauseButton");

    verify(playlistService, never()).pauseCurrentTrack();
    verify(playlistService, never()).resumeCurrentTrack();
    verify(playlistService, never()).playPlaylist(anyInt());
    verify(playlistService).playCurrentTrack(true);
  }

  @Test
  void shouldClickPlayPauseButtonWhenPlaying() {
    when(mediaService.isPlaying()).thenReturn(true);

    clickOnNode("#playPauseButton");

    verify(playlistService).pauseCurrentTrack();
    verify(playlistService, never()).resumeCurrentTrack();
    verify(playlistService, never()).playPlaylist(anyInt());
    verify(playlistService, never()).playCurrentTrack(true);
  }

  @Test
  void shouldClickPlayPauseButtonWhenPaused() {
    when(mediaService.isPaused()).thenReturn(true);

    clickOnNode("#playPauseButton");

    verify(playlistService, never()).pauseCurrentTrack();
    verify(playlistService).resumeCurrentTrack();
    verify(playlistService, never()).playPlaylist(anyInt());
    verify(playlistService, never()).playCurrentTrack(true);
  }

  @Test
  void shouldClickPlayPauseButtonWhenPlaylistSelected() {
    Playlist playlist = mock(Playlist.class);
    when(playlist.isEmpty()).thenReturn(false);
    when(playlistService.getPlaylist(anyInt())).thenReturn(of(playlist));
    when(playlistService.getSelectedTrack()).thenReturn(null);

    clickOnNode("#playPauseButton");

    verify(playlistService, never()).pauseCurrentTrack();
    verify(playlistService, never()).resumeCurrentTrack();
    verify(playlistService).playPlaylist(anyInt());
    verify(playlistService, never()).playCurrentTrack(true);
  }

  @Test
  void shouldClickPlayPauseButtonWhenPlaylistAndTrackSelected() {
    Playlist playlist = mock(Playlist.class);
    when(playlist.isEmpty()).thenReturn(false);
    when(playlistService.getPlaylist(anyInt())).thenReturn(of(playlist));
    when(playlistService.getSelectedTrack()).thenReturn(mock(Track.class));

    clickOnNode("#playPauseButton");

    verify(playlistService, never()).pauseCurrentTrack();
    verify(playlistService, never()).resumeCurrentTrack();
    verify(playlistService, never()).playPlaylist(anyInt());
    verify(playlistService).playCurrentTrack(true);
  }

  @Test
  void shouldClickNextButton() {
    clickOnNode("#nextButton");

    verify(playlistService).playNextTrack(true);
  }

  @ParameterizedTest
  @MethodSource("getVolumeCombinations")
  void shouldClickVolumeButton(boolean isMuted, String imageUrl) {
    when(mediaService.isMuted()).thenReturn(isMuted);

    clickOnNode("#volumeButton");

    verify(mediaService).setMuted();

    Button volumeButton = find("#volumeButton");
    assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + imageUrl + "')");
  }

  private static Stream<Arguments> getVolumeCombinations() {
    return Stream.of(
        Arguments.of(true, IMAGE_VOLUME_OFF),
        Arguments.of(false, IMAGE_VOLUME_ON)
    );
  }

  @ParameterizedTest
  @MethodSource("getShuffleCombinations")
  void shouldClickShuffleButton(boolean isShuffle, String imageUrl) {
    when(playlistService.isShuffle()).thenReturn(isShuffle);

    clickOnNode("#shuffleButton");

    verify(playlistService).setShuffle(!isShuffle, false);

    Button shuffleButton = find("#shuffleButton");
    assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + imageUrl + "')");
  }

  private static Stream<Arguments> getShuffleCombinations() {
    return Stream.of(
        Arguments.of(true, IMAGE_SHUFFLE_ON),
        Arguments.of(false, IMAGE_SHUFFLE_OFF)
    );
  }

  @ParameterizedTest
  @MethodSource("getRepeatCombinations")
  void shouldClickRepeatButton(Repeat repeat, String imageUrl) {
    when(playlistService.getRepeat()).thenReturn(repeat);

    clickOnNode("#repeatButton");

    verify(playlistService).updateRepeat();

    Button repeatButton = find("#repeatButton");
    assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + imageUrl + "')");
  }

  private static Stream<Arguments> getRepeatCombinations() {
    return Stream.of(
        Arguments.of(OFF, IMAGE_REPEAT_OFF),
        Arguments.of(ONE, IMAGE_REPEAT_ONE),
        Arguments.of(ALL, IMAGE_REPEAT_ALL)
    );
  }

  @Test
  void shouldClickEqButton() {
    clickOnNode("#eqButton");

    verify(equalizerController).updateSliderValues();
    verify(equalizerView).show(true);
  }

  @Test
  void shouldClickRandomButtonWithYearFilter() {
    String year = createYearString();

    @SuppressWarnings("unchecked")
    ComboBox<YearFilter> yearFilterComboBox = getNonNullField(underTest, "yearFilterComboBox", ComboBox.class);
    yearFilterComboBox.getItems().add(new YearFilter(year, year));

    Platform.runLater(() -> yearFilterComboBox.getSelectionModel().select(0));

    WaitForAsyncUtils.waitForFxEvents();

    @SuppressWarnings("unchecked")
    List<Track> tracks = (List<Track>) mock(List.class);

    when(searchService.getShuffledPlaylist(anyInt(), anyString())).thenReturn(tracks);

    clickOnNode("#randomButton");

    verify(searchService).getShuffledPlaylist(applicationProperties.getShuffledPlaylistSize(), year);
    verify(playlistService).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
    verify(playlistService).playPlaylist(PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldClickRandomButtonWithNoYearFilter() {
    String year = createYearString();

    @SuppressWarnings("unchecked")
    ComboBox<YearFilter> yearFilterComboBox = getNonNullField(underTest, "yearFilterComboBox", ComboBox.class);
    yearFilterComboBox.getItems().add(new YearFilter(year, year));

    Platform.runLater(() -> yearFilterComboBox.getSelectionModel().clearSelection());

    WaitForAsyncUtils.waitForFxEvents();

    @SuppressWarnings("unchecked")
    List<Track> tracks = (List<Track>) mock(List.class);

    when(searchService.getShuffledPlaylist(applicationProperties.getShuffledPlaylistSize(), null)).thenReturn(tracks);

    clickOnNode("#randomButton");

    verify(searchService).getShuffledPlaylist(applicationProperties.getShuffledPlaylistSize(), null);
    verify(playlistService).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
    verify(playlistService).playPlaylist(PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldFirePlaylistSelected() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10));
      playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10));
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playlistPanelListView.getItems()).hasSize(2);

    Platform.runLater(() -> playlistPanelListView.getSelectionModel().select(1));

    WaitForAsyncUtils.waitForFxEvents();

    verify(eventProcessor).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
  }

  ///////////////////////////
  // Keyboard Interactions //
  ///////////////////////////

  @Test
  @SneakyThrows
  void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithBackSpace() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Playlist playlist = new Playlist(1, createPlaylistName(), 10);

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(playlist);
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    playlistPanelListView.onKeyPressedProperty().get().handle(createKeyEvent(KEY_PRESSED, BACK_SPACE));

    // Wait for the UI thread
    Thread.sleep(250);

    ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

    verify(confirmView).setMessage(stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
    verify(confirmView).setRunnables(okRunnable.capture(), any());
    verify(confirmView).show(anyBoolean());

    okRunnable.getValue().run();

    verify(playlistService).deletePlaylist(playlist.getPlaylistId());
  }

  @Test
  @SneakyThrows
  void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithDelete() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Playlist playlist = new Playlist(1, createPlaylistName(), 10);

    Platform.runLater(() -> {
      playlistPanelListView.getItems().add(playlist);
      playlistPanelListView.getSelectionModel().select(0);
    });

    WaitForAsyncUtils.waitForFxEvents();

    playlistPanelListView.onKeyPressedProperty().get().handle(createKeyEvent(KEY_PRESSED, DELETE));

    // Wait for the UI thread
    Thread.sleep(250);

    ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

    verify(confirmView).setMessage(stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
    verify(confirmView).setRunnables(okRunnable.capture(), any());
    verify(confirmView).show(anyBoolean());

    okRunnable.getValue().run();

    verify(playlistService).deletePlaylist(playlist.getPlaylistId());
  }

  @Test
  @SneakyThrows
  void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithUnknownKey() {
    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

    playlistPanelListView.onKeyPressedProperty().get().handle(createKeyEvent(KEY_PRESSED, A));

    // Wait for the UI thread
    Thread.sleep(250);

    verify(confirmView, never()).show(anyBoolean());
  }

  //////////////////////
  // Event Processing //
  //////////////////////

  @Test
  void shouldReceiveApplicationInitialised() {
    find("#yearFilterComboBox").setDisable(true);
    find("#searchTextField").setDisable(true);
    find("#addPlaylistButton").setDisable(true);
    find("#deletePlaylistButton").setDisable(true);
    find("#importPlaylistButton").setDisable(true);
    find("#exportPlaylistButton").setDisable(true);
    find("#settingsButton").setDisable(true);
    find("#timeSlider").setDisable(true);
    find("#volumeButton").setDisable(true);
    find("#volumeSlider").setDisable(true);
    find("#shuffleButton").setDisable(true);
    find("#repeatButton").setDisable(true);
    find("#eqButton").setDisable(true);
    find("#randomButton").setDisable(true);

    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10),
        new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10)
    );
    when(playlistService.getPlaylists()).thenReturn(playlists);
    when(playlistService.getRepeat()).thenReturn(OFF);
    when(playlistService.isShuffle()).thenReturn(false);
    when(mediaService.isMuted()).thenReturn(false);
    when(searchService.getYearList()).thenReturn(null);

    Platform.runLater(() -> underTest.eventReceived(APPLICATION_INITIALISED));

    WaitForAsyncUtils.waitForFxEvents();

    ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
    YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
    assertThat(yearFilterComboBox.getItems()).hasSize(1);
    assertThat(yearFilter.year()).isNull();

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getField(underTest, "observablePlaylists", ObservableList.class);
    assertThat(observablePlaylists).hasSize(2);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    assertThat(playlistPanelListView.getSelectionModel().getSelectedIndex()).isEqualTo(0);
    assertThat(playlistPanelListView.getFocusModel().getFocusedIndex()).isEqualTo(0);

    Button volumeButton = find("#volumeButton");
    assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_VOLUME_ON + "')");

    Button shuffleButton = find("#shuffleButton");
    assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");

    Button repeatButton = find("#repeatButton");
    assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");

    assertThat(find("#yearFilterComboBox").isDisabled()).isFalse();
    assertThat(find("#searchTextField").isDisabled()).isFalse();
    assertThat(find("#addPlaylistButton").isDisabled()).isFalse();
    assertThat(find("#deletePlaylistButton").isDisabled()).isFalse();
    assertThat(find("#importPlaylistButton").isDisabled()).isFalse();
    assertThat(find("#exportPlaylistButton").isDisabled()).isFalse();
    assertThat(find("#settingsButton").isDisabled()).isFalse();
    assertThat(find("#timeSlider").isDisabled()).isFalse();
    assertThat(find("#volumeButton").isDisabled()).isFalse();
    assertThat(find("#volumeSlider").isDisabled()).isFalse();
    assertThat(find("#shuffleButton").isDisabled()).isFalse();
    assertThat(find("#repeatButton").isDisabled()).isFalse();
    assertThat(find("#eqButton").isDisabled()).isFalse();
    assertThat(find("#randomButton").isDisabled()).isFalse();
  }

  @Test
  void shouldReceiveApplicationInitialisedWithEmptyYearList() {
    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10),
        new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10)
    );
    when(playlistService.getPlaylists()).thenReturn(playlists);
    when(playlistService.getRepeat()).thenReturn(OFF);
    when(playlistService.isShuffle()).thenReturn(false);
    when(mediaService.isMuted()).thenReturn(false);
    when(searchService.getYearList()).thenReturn(emptyList());

    Platform.runLater(() -> underTest.eventReceived(APPLICATION_INITIALISED));

    WaitForAsyncUtils.waitForFxEvents();

    ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
    YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
    assertThat(yearFilterComboBox.getItems()).hasSize(1);
    assertThat(yearFilter.year()).isNull();
  }

  @Test
  void shouldReceiveApplicationInitialisedWithYearList() {
    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10),
        new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10)
    );
    when(playlistService.getPlaylists()).thenReturn(playlists);
    when(playlistService.getRepeat()).thenReturn(OFF);
    when(playlistService.isShuffle()).thenReturn(false);
    when(mediaService.isMuted()).thenReturn(false);
    when(searchService.getYearList()).thenReturn(List.of(createYearString(), createYearString()));

    Platform.runLater(() -> underTest.eventReceived(APPLICATION_INITIALISED));

    WaitForAsyncUtils.waitForFxEvents();

    ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
    YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
    assertThat(yearFilterComboBox.getItems()).hasSize(3);
    assertThat(yearFilter.year()).isNull();
  }

  @Test
  void shouldReceiveApplicationInitialisedWithNoPlaylists() {
    when(playlistService.getPlaylists()).thenReturn(emptyList());
    when(playlistService.getRepeat()).thenReturn(OFF);
    when(playlistService.isShuffle()).thenReturn(false);
    when(mediaService.isMuted()).thenReturn(false);
    when(searchService.getYearList()).thenReturn(null);

    Platform.runLater(() -> underTest.eventReceived(APPLICATION_INITIALISED));

    WaitForAsyncUtils.waitForFxEvents();

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getField(underTest, "observablePlaylists", ObservableList.class);
    assertThat(observablePlaylists).isEmpty();
  }

  @Test
  void shouldReceiveDataIndexed() {
    Platform.runLater(() -> underTest.eventReceived(DATA_INDEXED));

    WaitForAsyncUtils.waitForFxEvents();

    verify(underTest).updateYearFilter();
  }

  @Test
  void shouldReceiveNewVersionAvailable() {
    Button newVersionButton = find("#newVersionButton");
    Version version = createVersion();

    Platform.runLater(() -> {
      newVersionButton.setText(null);
      newVersionButton.setDisable(true);
      newVersionButton.setVisible(false);

      underTest.eventReceived(NEW_VERSION_AVAILABLE, version);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(newVersionButton.getText()).isEqualTo(stringResourceService.getString(MESSAGE_NEW_VERSION_AVAILABLE, version));
    assertThat(newVersionButton.isDisabled()).isFalse();
    assertThat(newVersionButton.isVisible()).isTrue();
  }

  @Test
  void shouldReceiveMuteUpdated() {
    Platform.runLater(() -> underTest.eventReceived(MUTE_UPDATED));

    WaitForAsyncUtils.waitForFxEvents();

    verify(underTest).setVolumeButtonImage();
  }

  @Test
  void shouldReceiveTimeUpdated() {
    Duration mediaDuration = new Duration(30000);
    Duration currentTime = new Duration(15000);
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      timeSlider.setDisable(true);
      playTimeLabel.setText(null);

      underTest.eventReceived(TIME_UPDATED, mediaDuration, currentTime);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.isDisabled()).isFalse();
    assertThat(timeSlider.getSliderValue()).isEqualTo(50.0d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:15/00:30");
  }

  @Test
  void shouldReceiveTimeUpdatedMediaDurationUnknown() {
    Duration mediaDuration = Duration.UNKNOWN;
    Duration currentTime = new Duration(15000);
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      timeSlider.setDisable(false);
      playTimeLabel.setText(null);

      underTest.eventReceived(TIME_UPDATED, mediaDuration, currentTime);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.isDisabled()).isTrue();
    assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:15");
  }

  @Test
  void shouldReceiveTimeUpdatedZeroMediaDuration() {
    Duration mediaDuration = Duration.ZERO;
    Duration currentTime = new Duration(15000);
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      timeSlider.setDisable(false);
      playTimeLabel.setText(null);

      underTest.eventReceived(TIME_UPDATED, mediaDuration, currentTime);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.isDisabled()).isFalse();
    assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:15");
  }

  @Test
  void shouldReceiveBufferUpdated() {
    Duration mediaDuration = new Duration(30000);
    Duration bufferProgressTime = new Duration(15000);
    SliderProgressBar timeSlider = find("#timeSlider");

    Platform.runLater(() -> {
      timeSlider.setProgressValue(0);

      underTest.eventReceived(BUFFER_UPDATED, mediaDuration, bufferProgressTime);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.getProgressValue()).isEqualTo(0.5d);
  }

  @Test
  void shouldReceiveBufferUpdatedWithNullMediaDuration() {
    Duration bufferProgressTime = new Duration(15000);
    SliderProgressBar timeSlider = find("#timeSlider");

    Platform.runLater(() -> {
      timeSlider.setProgressValue(0);

      underTest.eventReceived(BUFFER_UPDATED, null, bufferProgressTime);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
  }

  @Test
  void shouldReceiveBufferUpdatedWithNullBufferProgressTime() {
    Duration mediaDuration = new Duration(30000);
    SliderProgressBar timeSlider = find("#timeSlider");

    Platform.runLater(() -> {
      timeSlider.setProgressValue(0);

      underTest.eventReceived(BUFFER_UPDATED, mediaDuration, null);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
  }

  @Test
  void shouldReceiveMediaPlaying() {
    Button playPauseButton = find("#playPauseButton");
    Button previousButton = find("#previousButton");
    Button nextButton = find("#nextButton");

    Platform.runLater(() -> {
      playPauseButton.setStyle(null);
      playPauseButton.setDisable(true);
      previousButton.setDisable(true);
      nextButton.setDisable(true);

      underTest.eventReceived(MEDIA_PLAYING);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PAUSE + "')");
    assertThat(playPauseButton.isDisabled()).isFalse();
    assertThat(previousButton.isDisabled()).isFalse();
    assertThat(nextButton.isDisabled()).isFalse();
  }

  @Test
  void shouldReceiveMediaPaused() {
    Button playPauseButton = find("#playPauseButton");
    Button previousButton = find("#previousButton");
    Button nextButton = find("#nextButton");

    Platform.runLater(() -> {
      playPauseButton.setStyle(null);
      playPauseButton.setDisable(true);
      previousButton.setDisable(false);
      nextButton.setDisable(false);

      underTest.eventReceived(MEDIA_PAUSED);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
    assertThat(playPauseButton.isDisabled()).isFalse();
    assertThat(previousButton.isDisabled()).isTrue();
    assertThat(nextButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceiveMediaStopped() {
    Button playPauseButton = find("#playPauseButton");
    Button previousButton = find("#previousButton");
    Button nextButton = find("#nextButton");
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      playPauseButton.setStyle(null);
      playPauseButton.setDisable(false);
      previousButton.setDisable(false);
      nextButton.setDisable(false);
      timeSlider.setSliderValue(99);
      timeSlider.setProgressValue(99);
      playTimeLabel.setText(null);

      underTest.eventReceived(MEDIA_STOPPED);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
    assertThat(playPauseButton.isDisabled()).isFalse();
    assertThat(previousButton.isDisabled()).isTrue();
    assertThat(nextButton.isDisabled()).isTrue();
    assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
    assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
  }

  @Test
  void shouldReceiveEndOfMedia() {
    when(playlistService.getRepeat()).thenReturn(OFF);

    Button playPauseButton = find("#playPauseButton");
    Button previousButton = find("#previousButton");
    Button nextButton = find("#nextButton");
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      playPauseButton.setStyle(null);
      playPauseButton.setDisable(false);
      previousButton.setDisable(false);
      nextButton.setDisable(false);
      timeSlider.setSliderValue(99);
      timeSlider.setProgressValue(99);
      playTimeLabel.setText(null);

      underTest.eventReceived(END_OF_MEDIA);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
    assertThat(playPauseButton.isDisabled()).isFalse();
    assertThat(previousButton.isDisabled()).isTrue();
    assertThat(nextButton.isDisabled()).isTrue();
    assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
    assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
  }

  @Test
  void shouldReceiveEndOfMediaWithRepeatOne() {
    when(playlistService.getRepeat()).thenReturn(ONE);

    Button playPauseButton = find("#playPauseButton");
    Button previousButton = find("#previousButton");
    Button nextButton = find("#nextButton");
    SliderProgressBar timeSlider = find("#timeSlider");
    Label playTimeLabel = find("#playTimeLabel");

    Platform.runLater(() -> {
      playPauseButton.setStyle(null);
      playPauseButton.setDisable(false);
      previousButton.setDisable(false);
      nextButton.setDisable(false);
      timeSlider.setSliderValue(99);
      timeSlider.setProgressValue(99);
      playTimeLabel.setText(null);

      underTest.eventReceived(END_OF_MEDIA);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
    assertThat(playPauseButton.isDisabled()).isFalse();
    assertThat(previousButton.isDisabled()).isTrue();
    assertThat(nextButton.isDisabled()).isTrue();
    assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
    assertThat(timeSlider.getProgressValue()).isEqualTo(0.99d);
    assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
  }

  @Test
  void shouldReceivePlaylistSelected() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest, never()).updateObservablePlaylists();
    verify(playlistService).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(playPauseButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceivePlaylistSelectedWithNullPayload() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_SELECTED, (Object[]) null);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest, never()).updateObservablePlaylists();
    verify(playlistService, never()).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
    assertThat(playPauseButton.isDisabled()).isFalse();
  }

  @Test
  void shouldReceivePlaylistSelectedWithEmptyPayload() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_SELECTED);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest, never()).updateObservablePlaylists();
    verify(playlistService, never()).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
    assertThat(playPauseButton.isDisabled()).isFalse();
  }

  @Test
  void shouldReceivePlaylistSelectedExistingPlaylist() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest, never()).updateObservablePlaylists();
    verify(playlistService, never()).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(search);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(search);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
    assertThat(playPauseButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceivePlaylistSelectedPlaylistIsNotEmpty() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    favourites.addTrack(mock(Track.class));
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(true);

      underTest.eventReceived(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest, never()).updateObservablePlaylists();
    verify(playlistService).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(playPauseButton.isDisabled()).isFalse();
  }

  @Test
  void shouldReceivePlaylistCreatedWithEdit() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, true);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest).updateObservablePlaylists();
    verify(playlistService).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(playPauseButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceivePlaylistCreatedWithoutEdit() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, false);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest).updateObservablePlaylists();
    verify(playlistService).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(playPauseButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceivePlaylistDeleted() {
    setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

    Playlist search = new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10);
    Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10);
    when(playlistService.getPlaylists()).thenReturn(List.of(search, favourites));
    when(playlistService.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
    when(playlistService.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);

    @SuppressWarnings("unchecked")
    ObservableList<Playlist> observablePlaylists = getNonNullField(underTest, "observablePlaylists", ObservableList.class);

    ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      observablePlaylists.add(search);
      observablePlaylists.add(favourites);
      playlistPanelListView.getSelectionModel().clearSelection();
      playlistPanelListView.getFocusModel().focus(-1);
      playPauseButton.setDisable(false);

      underTest.eventReceived(PLAYLIST_DELETED, PLAYLIST_ID_FAVOURITES);
    });

    WaitForAsyncUtils.waitForFxEvents();

    int currentSelectedPlaylistId = getNonNullField(underTest, "currentSelectedPlaylistId", Integer.class);

    verify(underTest).updateObservablePlaylists();
    verify(playlistService).clearSelectedTrack();

    assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
    assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
    assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(playPauseButton.isDisabled()).isTrue();
  }

  @Test
  void shouldReceiveTrackSelected() {
    Button playPauseButton = find("#playPauseButton");

    Platform.runLater(() -> {
      playPauseButton.setDisable(true);

      underTest.eventReceived(TRACK_SELECTED);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playPauseButton.isDisabled()).isFalse();
  }

  @Test
  void shouldReceiveTrackQueuedForPlaying() {
    Button playPauseButton = find("#playPauseButton");
    ImageView playingImageView = find("#playingImageView");
    Label playingTrackLabel = find("#playingTrackLabel");
    Label playingAlbumLabel = find("#playingAlbumLabel");
    Label playingArtistLabel = find("#playingArtistLabel");
    Track track = createTrack(1);

    String albumImageUrl = "http://www.example.com/image.png";
    Image albumImage = new Image(albumImageUrl);

    when(cacheService.constructInternalUrl(any(), anyString(), anyString())).thenReturn(albumImageUrl);
    doAnswer(invocation -> {
      ImageView imageView = invocation.getArgument(0);
      imageView.setImage(albumImage);

      return null;
    }).when(imageFactory).loadImage(playingImageView, albumImageUrl);

    Platform.runLater(() -> {
      playPauseButton.setDisable(false);
      playingImageView.setImage(null);
      playingTrackLabel.setText(null);
      playingAlbumLabel.setText(null);
      playingArtistLabel.setText(null);

      underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, track);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
    assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
    assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
    assertThat(playingImageView.getImage()).isNotNull();
    assertThat(playPauseButton.isDisabled()).isTrue();

    verify(nativeService).displayNotification(track);
    verify(imageFactory).loadImage(playingImageView, albumImageUrl);
  }

  @Test
  void shouldReceiveTrackQueuedForPlayingNullPayload() {
    Button playPauseButton = find("#playPauseButton");
    ImageView playingImageView = find("#playingImageView");
    Label playingTrackLabel = find("#playingTrackLabel");
    Label playingAlbumLabel = find("#playingAlbumLabel");
    Label playingArtistLabel = find("#playingArtistLabel");
    Track track = createTrack(1);

    when(cacheService.constructInternalUrl(any(), anyString(), anyString())).thenReturn("http://www.example.com/image.png");

    Platform.runLater(() -> {
      playPauseButton.setDisable(false);
      playingImageView.setImage(null);
      playingTrackLabel.setText(null);
      playingAlbumLabel.setText(null);
      playingArtistLabel.setText(null);

      underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, (Object[]) null);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playingTrackLabel.getText()).isNull();
    assertThat(playingAlbumLabel.getText()).isNull();
    assertThat(playingArtistLabel.getText()).isNull();
    assertThat(playingImageView.getImage()).isNull();
    assertThat(playPauseButton.isDisabled()).isFalse();

    verify(nativeService, never()).displayNotification(track);
  }

  @Test
  void shouldReceiveTrackQueuedForPlayingEmptyPayload() {
    Button playPauseButton = find("#playPauseButton");
    ImageView playingImageView = find("#playingImageView");
    Label playingTrackLabel = find("#playingTrackLabel");
    Label playingAlbumLabel = find("#playingAlbumLabel");
    Label playingArtistLabel = find("#playingArtistLabel");
    Track track = createTrack(1);

    when(cacheService.constructInternalUrl(any(), anyString(), anyString())).thenReturn("http://www.example.com/image.png");

    Platform.runLater(() -> {
      playPauseButton.setDisable(false);
      playingImageView.setImage(null);
      playingTrackLabel.setText(null);
      playingAlbumLabel.setText(null);
      playingArtistLabel.setText(null);

      underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING);
    });

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(playingTrackLabel.getText()).isNull();
    assertThat(playingAlbumLabel.getText()).isNull();
    assertThat(playingArtistLabel.getText()).isNull();
    assertThat(playingImageView.getImage()).isNull();
    assertThat(playPauseButton.isDisabled()).isFalse();

    verify(nativeService, never()).displayNotification(track);
  }

  @Test
  void shouldReceiveMenuFileImportPlaylist() {
    doNothing().when(underTest).handleImportPlaylistButtonAction();

    underTest.eventReceived(MENU_FILE_IMPORT_PLAYLIST);

    verify(underTest).handleImportPlaylistButtonAction();
  }

  @Test
  void shouldReceiveMenuFileExportPlaylist() {
    doNothing().when(underTest).handleExportPlaylistButtonAction();

    underTest.eventReceived(MENU_FILE_EXPORT_PLAYLIST);

    verify(underTest).handleExportPlaylistButtonAction();
  }

  @Test
  void shouldReceiveMenuFileSettings() {
    doNothing().when(underTest).handleSettingsButtonAction();

    underTest.eventReceived(MENU_FILE_SETTINGS);

    verify(underTest).handleSettingsButtonAction();
  }

  @Test
  void shouldReceiveMenuEditAddPlaylist() {
    doNothing().when(underTest).handleAddPlaylistButtonAction();

    underTest.eventReceived(MENU_EDIT_ADD_PLAYLIST);

    verify(underTest).handleAddPlaylistButtonAction();
  }

  @Test
  void shouldReceiveMenuEditDeletePlaylist() {
    doNothing().when(underTest).handleDeletePlaylistButtonAction();

    underTest.eventReceived(MENU_EDIT_DELETE_PLAYLIST);

    verify(underTest).handleDeletePlaylistButtonAction();
  }

  @Test
  void shouldReceiveEditCreatePlaylistFromAlbumWithSelectedTrack() {
    Track mockTrack = mock(Track.class);

    when(trackTableController.getSelectedTrack()).thenReturn(mockTrack);

    underTest.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

    verify(playlistService).createPlaylistFromAlbum(mockTrack);
  }

  @Test
  void shouldReceiveEditCreatePlaylistFromAlbumWithoutSelectedTrack() {
    when(trackTableController.getSelectedTrack()).thenReturn(null);

    underTest.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

    verify(playlistService, never()).createPlaylistFromAlbum(any());
  }

  @Test
  void shouldReceiveMenuEditRandomPlaylist() {
    doNothing().when(underTest).handleRandomButtonAction();

    underTest.eventReceived(MENU_EDIT_RANDOM_PLAYLIST);

    verify(underTest).handleRandomButtonAction();
  }

  @Test
  void shouldReceiveMenuControlsPlayPause() {
    doNothing().when(underTest).handlePlayPauseButtonAction();

    underTest.eventReceived(MENU_CONTROLS_PLAY_PAUSE);

    verify(underTest).handlePlayPauseButtonAction();
  }

  @Test
  void shouldReceiveMenuControlsPrevious() {
    doNothing().when(underTest).handlePreviousButtonAction();

    underTest.eventReceived(MENU_CONTROLS_PREVIOUS);

    verify(underTest).handlePreviousButtonAction();
  }

  @Test
  void shouldReceiveMenuControlsNext() {
    doNothing().when(underTest).handleNextButtonAction();

    underTest.eventReceived(MENU_CONTROLS_NEXT);

    verify(underTest).handleNextButtonAction();
  }

  @Test
  void shouldReceiveMenuControlsShuffle() {
    doNothing().when(underTest).setShuffleButtonImage();

    underTest.eventReceived(MENU_CONTROLS_SHUFFLE);

    verify(underTest).setShuffleButtonImage();
  }

  @Test
  void shouldReceiveMenuControlsRepeat() {
    doNothing().when(underTest).setRepeatButtonImage();

    underTest.eventReceived(MENU_CONTROLS_REPEAT);

    verify(underTest).setRepeatButtonImage();
  }

  @Test
  void shouldReceiveMenuControlsVolumeUpWithPayload() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(10d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

    assertThat(volumeSlider.getValue()).isEqualTo(20d);
    verify(mediaService).setVolumePercent(20d);
  }

  @Test
  void shouldReceiveMenuControlsVolumeUpWithPayloadOver100() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(95d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

    assertThat(volumeSlider.getValue()).isEqualTo(100d);
    verify(mediaService).setVolumePercent(100d);
  }

  @Test
  void shouldReceiveMenuControlsVolumeUpWithoutPayload() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(10d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_UP);

    assertThat(volumeSlider.getValue()).isEqualTo(10d);
    verify(mediaService, never()).setVolumePercent(anyDouble());
  }

  @Test
  void shouldReceiveMenuControlsVolumeDownWithPayload() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(90d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

    assertThat(volumeSlider.getValue()).isEqualTo(80d);
    verify(mediaService).setVolumePercent(80d);
  }

  @Test
  void shouldReceiveMenuControlsVolumeDownWithPayloadBelowZero() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(5d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

    assertThat(volumeSlider.getValue()).isEqualTo(0d);
    verify(mediaService).setVolumePercent(0d);
  }

  @Test
  void shouldReceiveMenuControlsVolumeDownWithoutPayload() {
    Slider volumeSlider = find("#volumeSlider");
    volumeSlider.setValue(10d);

    underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN);

    assertThat(volumeSlider.getValue()).isEqualTo(10d);
    verify(mediaService, never()).setVolumePercent(anyDouble());
  }

  @Test
  void shouldReceiveMenuControlsVolumeMute() {
    doNothing().when(underTest).handleVolumeButtonAction();

    underTest.eventReceived(MENU_CONTROLS_VOLUME_MUTE);

    verify(underTest).handleVolumeButtonAction();
  }

  @Test
  void shouldReceiveMenuViewEqualizer() {
    doNothing().when(underTest).handleEqButtonAction();

    underTest.eventReceived(MENU_VIEW_EQUALIZER);

    verify(underTest).handleEqButtonAction();
  }
}