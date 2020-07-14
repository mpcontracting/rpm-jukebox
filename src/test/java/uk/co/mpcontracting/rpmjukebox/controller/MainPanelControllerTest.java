package uk.co.mpcontracting.rpmjukebox.controller;

import com.google.gson.Gson;
import com.igormaznitsa.commons.version.Version;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.component.ImageFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.javafx.GUIState;
import uk.co.mpcontracting.rpmjukebox.manager.*;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.event.Event.*;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class MainPanelControllerTest extends AbstractGUITest implements Constants {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private MainPanelController mainPanelController;

    @Autowired
    private MainPanelView mainPanelView;

    @Autowired
    private MessageManager messageManager;

    @Mock
    private ImageFactory mockImageFactory;

    @Mock
    private EqualizerView mockEqualizerView;

    @Mock
    private SettingsView mockSettingsView;

    @Mock
    private ExportView mockExportView;

    @Mock
    private MessageView mockMessageView;

    @Mock
    private ConfirmView mockConfirmView;

    @Mock
    private TrackTableView mockTrackTableView;

    @Mock
    private TrackTableController mockTrackTableController;

    @Mock
    private EqualizerController mockEqualizerController;

    @Mock
    private SettingsController mockSettingsController;

    @Mock
    private ExportController mockExportController;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private PlaylistManager mockPlaylistManager;

    @Mock
    private MediaManager mockMediaManager;

    @Mock
    private CacheManager mockCacheManager;

    @Mock
    private NativeManager mockNativeManager;

    @Mock
    private UpdateManager mockUpdateManager;

    private Stage existingStage;
    private Parent mockRoot;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(mainPanelView);
    }

    @Before
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void setup() {
        existingStage = GUIState.getStage();

        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        mockRoot = mock(Parent.class);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(mockRoot);
        setField(GUIState.class, "stage", mockStage);

        setField(mainPanelController, "imageFactory", mockImageFactory);
        setField(mainPanelController, "eventManager", getMockEventManager());
        setField(mainPanelController, "equalizerView", mockEqualizerView);
        setField(mainPanelController, "settingsView", mockSettingsView);
        setField(mainPanelController, "exportView", mockExportView);
        setField(mainPanelController, "messageView", mockMessageView);
        setField(mainPanelController, "confirmView", mockConfirmView);
        setField(mainPanelController, "trackTableView", mockTrackTableView);
        setField(mainPanelController, "trackTableController", mockTrackTableController);
        setField(mainPanelController, "equalizerController", mockEqualizerController);
        setField(mainPanelController, "settingsController", mockSettingsController);
        setField(mainPanelController, "exportController", mockExportController);
        setField(mainPanelController, "settingsManager", mockSettingsManager);
        setField(mainPanelController, "searchManager", mockSearchManager);
        setField(mainPanelController, "playlistManager", mockPlaylistManager);
        setField(mainPanelController, "mediaManager", mockMediaManager);
        setField(mainPanelController, "cacheManager", mockCacheManager);
        setField(mainPanelController, "nativeManager", mockNativeManager);
        setField(mainPanelController, "updateManager", mockUpdateManager);

        threadRunner.runOnGui(() -> {
            ((TextField) getNonNullField(mainPanelController, "searchTextField")).setText(null);
            ((ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox")).getItems().clear();
            ((ListView<Playlist>) getNonNullField(mainPanelController, "playlistPanelListView")).getItems().clear();
        });

        WaitForAsyncUtils.waitForFxEvents();

        reset(mockSearchManager);
        reset(mockPlaylistManager);
    }

    @Test
    @SneakyThrows
    public void shouldShowMessageView() {
        mainPanelController.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).setMessage("Message");
        verify(mockMessageView, times(1)).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldShowMessageViewAlreadyShowing() {
        when(mockMessageView.isShowing()).thenReturn(true);

        mainPanelController.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).setMessage("Message");
        verify(mockMessageView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldCloseMessageView() {
        mainPanelController.closeMessageView();

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).close();
    }

    @Test
    @SneakyThrows
    public void shouldShowConfirmView() {
        Runnable okRunnable = mock(Runnable.class);
        Runnable cancelRunnable = mock(Runnable.class);

        mainPanelController.showConfirmView("Message", true, okRunnable, cancelRunnable);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockConfirmView, times(1)).setMessage("Message");
        verify(mockConfirmView, times(1)).setRunnables(okRunnable, cancelRunnable);
        verify(mockConfirmView, times(1)).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldShowConfirmViewAlreadyShowing() {
        when(mockConfirmView.isShowing()).thenReturn(true);

        Runnable okRunnable = mock(Runnable.class);
        Runnable cancelRunnable = mock(Runnable.class);

        mainPanelController.showConfirmView("Message", true, okRunnable, cancelRunnable);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockConfirmView, times(1)).setMessage("Message");
        verify(mockConfirmView, times(1)).setRunnables(okRunnable, cancelRunnable);
        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldUpdateSearchTextSearchCriteria() {
        TextField searchTextField = (TextField) getNonNullField(mainPanelController, "searchTextField");

        threadRunner.runOnGui(() -> searchTextField.setText("Search"));

        WaitForAsyncUtils.waitForFxEvents();

        TrackSearch trackSearch = new TrackSearch("Search");

        verify(mockSearchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdateYearFilterSearchCriteria() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(1));

        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, Collections.emptyList());
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdateYearFilterAndSearchTextSearchCriteria() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        TextField searchTextField = (TextField) getNonNullField(mainPanelController, "searchTextField");

        threadRunner.runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(1);

            reset(mockPlaylistManager);

            searchTextField.setText("Search");
        });

        WaitForAsyncUtils.waitForFxEvents();

        TrackSearch trackSearch = new TrackSearch("Search", new TrackFilter(null, "2001"));

        verify(mockSearchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdatePlayingPlaylistOnYearFilterUpdate() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = mock(List.class);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);
        when(mockPlaylist.getTracks()).thenReturn(mockTracks);
        when(mockPlaylistManager.getPlayingPlaylist()).thenReturn(mockPlaylist);

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(1));

        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, mockTracks);
        verify(mockPlaylist, times(1)).getTracks();
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldClickNewVersionButton() {
        clickOnNode("#newVersionButton");

        verify(mockUpdateManager, times(1)).downloadNewVersion();
    }

    @Test
    public void shouldClickAddPlaylistButton() {
        clickOnNode("#addPlaylistButton");

        verify(mockPlaylistManager, times(1)).createPlaylist();
    }

    @Test
    @SneakyThrows
    public void shouldClickDeletePlaylistButton() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOnNode("#deletePlaylistButton");

        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mockConfirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(mockConfirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(mockConfirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(mockPlaylistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
    }

    @Test
    @SneakyThrows
    public void shouldClickDeletePlaylistButtonWithReservedPlaylist() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(PLAYLIST_ID_SEARCH, "Playlist 1", 10);

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        clickOnNode("#deletePlaylistButton");

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldClickDeletePlaylistButtonWithNullPlaylist() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> playlistPanelListView.getSelectionModel().clearSelection());

        WaitForAsyncUtils.waitForFxEvents();

        clickOnNode("#deletePlaylistButton");

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButton() {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        FileReader mockFileReader = mock(FileReader.class);
        doReturn(mockFileReader).when(spyMainPanelController).constructFileReader(any());

        Gson mockGson = mock(Gson.class);
        when(mockSettingsManager.getGson()).thenReturn(mockGson);

        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            Track mockTrack = mock(Track.class);
            when(mockTrack.getTrackId()).thenReturn(Integer.toString(i));

            playlist.addTrack(mockTrack);
            when(mockSearchManager.getTrackById(Integer.toString(i))).thenReturn(of(mockTrack));
        }

        List<PlaylistSettings> playlistSettings = new ArrayList<>();
        playlistSettings.add(new PlaylistSettings(playlist));

        when(mockGson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(playlistSettings);

        threadRunner.runOnGui(spyMainPanelController::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

        verify(mockPlaylistManager, times(1)).addPlaylist(playlistCaptor.capture());

        Playlist result = playlistCaptor.getValue();

        assertThat(result).isEqualTo(playlist);
        assertThat(result.getTracks()).hasSize(playlist.getTracks().size());

        verify(mockPlaylistManager, times(1)).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullTracksFromSearch() {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        FileReader mockFileReader = mock(FileReader.class);
        doReturn(mockFileReader).when(spyMainPanelController).constructFileReader(any());

        Gson mockGson = mock(Gson.class);
        when(mockSettingsManager.getGson()).thenReturn(mockGson);

        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            Track mockTrack = mock(Track.class);
            when(mockTrack.getTrackId()).thenReturn(Integer.toString(i));

            playlist.addTrack(mockTrack);
        }

        List<PlaylistSettings> playlistSettings = new ArrayList<>();
        playlistSettings.add(new PlaylistSettings(playlist));

        when(mockGson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(playlistSettings);
        when(mockSearchManager.getTrackById(any())).thenReturn(empty());

        threadRunner.runOnGui(spyMainPanelController::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

        verify(mockPlaylistManager, times(1)).addPlaylist(playlistCaptor.capture());

        Playlist result = playlistCaptor.getValue();

        assertThat(result).isEqualTo(playlist);
        assertThat(result.getTracks()).isEmpty();

        verify(mockPlaylistManager, times(1)).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullPlaylistSettings() {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        FileReader mockFileReader = mock(FileReader.class);
        doReturn(mockFileReader).when(spyMainPanelController).constructFileReader(any());

        Gson mockGson = mock(Gson.class);
        when(mockSettingsManager.getGson()).thenReturn(mockGson);
        when(mockGson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(null);

        threadRunner.runOnGui(spyMainPanelController::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPlaylistManager, never()).addPlaylist(any());
        verify(mockPlaylistManager, never()).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullFile() {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
        when(mockFileChooser.showOpenDialog(any())).thenReturn(null);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        threadRunner.runOnGui(spyMainPanelController::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPlaylistManager, never()).addPlaylist(any());
        verify(mockPlaylistManager, never()).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWhenExceptionThrown() {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        doThrow(new RuntimeException("MainPanelControllerTest.shouldClickImportPlaylistButtonWhenExceptionThrown()"))
                .when(spyMainPanelController).constructFileReader(any());

        threadRunner.runOnGui(spyMainPanelController::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPlaylistManager, never()).addPlaylist(any());
        verify(mockPlaylistManager, never()).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    public void shouldClickExportPlaylistButton() {
        clickOnNode("#exportPlaylistButton");

        verify(mockExportController, times(1)).bindPlaylists();
        verify(mockExportView, times(1)).show(true);
    }

    @Test
    public void shouldClickSettingsButton() {
        clickOnNode("#settingsButton");

        verify(mockSettingsController, times(1)).bindSystemSettings();
        verify(mockSettingsView, times(1)).show(true);
    }

    @Test
    public void shouldClickPreviousButton() {
        clickOnNode("#previousButton");

        verify(mockMediaManager, never()).setSeekPositionPercent(0d);
        verify(mockPlaylistManager, times(1)).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPreviousButtonWhenPlayingLessThanEqualCutoff() {
        when(mockMediaManager.getPlayingTimeSeconds()).thenReturn((double) appProperties.getPreviousSecondsCutoff());

        clickOnNode("#previousButton");

        verify(mockMediaManager, never()).setSeekPositionPercent(0d);
        verify(mockPlaylistManager, times(1)).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPreviousButtonWhenPlayingGreaterThanCutoff() {
        when(mockMediaManager.getPlayingTimeSeconds()).thenReturn(appProperties.getPreviousSecondsCutoff() + 1d);

        clickOnNode("#previousButton");

        verify(mockMediaManager, times(1)).setSeekPositionPercent(0d);
        verify(mockPlaylistManager, never()).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButton() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.isEmpty()).thenReturn(true);
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(of(mockPlaylist));

        clickOnNode("#playPauseButton");

        verify(mockPlaylistManager, never()).pauseCurrentTrack();
        verify(mockPlaylistManager, never()).resumeCurrentTrack();
        verify(mockPlaylistManager, never()).playPlaylist(anyInt());
        verify(mockPlaylistManager, times(1)).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(true);

        clickOnNode("#playPauseButton");

        verify(mockPlaylistManager, times(1)).pauseCurrentTrack();
        verify(mockPlaylistManager, never()).resumeCurrentTrack();
        verify(mockPlaylistManager, never()).playPlaylist(anyInt());
        verify(mockPlaylistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPaused() {
        when(mockMediaManager.isPaused()).thenReturn(true);

        clickOnNode("#playPauseButton");

        verify(mockPlaylistManager, never()).pauseCurrentTrack();
        verify(mockPlaylistManager, times(1)).resumeCurrentTrack();
        verify(mockPlaylistManager, never()).playPlaylist(anyInt());
        verify(mockPlaylistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaylistSelected() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.isEmpty()).thenReturn(false);
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(of(mockPlaylist));
        when(mockPlaylistManager.getSelectedTrack()).thenReturn(null);

        clickOnNode("#playPauseButton");

        verify(mockPlaylistManager, never()).pauseCurrentTrack();
        verify(mockPlaylistManager, never()).resumeCurrentTrack();
        verify(mockPlaylistManager, times(1)).playPlaylist(anyInt());
        verify(mockPlaylistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaylistAndTrackSelected() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.isEmpty()).thenReturn(false);
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(of(mockPlaylist));
        when(mockPlaylistManager.getSelectedTrack()).thenReturn(mock(Track.class));

        clickOnNode("#playPauseButton");

        verify(mockPlaylistManager, never()).pauseCurrentTrack();
        verify(mockPlaylistManager, never()).resumeCurrentTrack();
        verify(mockPlaylistManager, never()).playPlaylist(anyInt());
        verify(mockPlaylistManager, times(1)).playCurrentTrack(true);
    }

    @Test
    public void shouldClickNextButton() {
        clickOnNode("#nextButton");

        verify(mockPlaylistManager, times(1)).playNextTrack(true);
    }

    @Test
    public void shouldClickVolumeButtonWhenMuted() {
        when(mockMediaManager.isMuted()).thenReturn(true);

        clickOnNode("#volumeButton");

        verify(mockMediaManager, times(1)).setMuted();

        Button volumeButton = find("#volumeButton");
        assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_VOLUME_OFF + "')");
    }

    @Test
    public void shouldClickVolumeButtonWhenNotMuted() {
        when(mockMediaManager.isMuted()).thenReturn(false);

        clickOnNode("#volumeButton");

        verify(mockMediaManager, times(1)).setMuted();

        Button volumeButton = find("#volumeButton");
        assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_VOLUME_ON + "')");
    }

    @Test
    public void shouldClickShuffleButtonWhenShuffled() {
        when(mockPlaylistManager.isShuffle()).thenReturn(true);

        clickOnNode("#shuffleButton");

        verify(mockPlaylistManager, times(1)).setShuffle(false, false);

        Button shuffleButton = find("#shuffleButton");
        assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
    }

    @Test
    public void shouldClickShuffleButtonWhenNotShuffled() {
        when(mockPlaylistManager.isShuffle()).thenReturn(false);

        clickOnNode("#shuffleButton");

        verify(mockPlaylistManager, times(1)).setShuffle(true, false);

        Button shuffleButton = find("#shuffleButton");
        assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatOff() {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);

        clickOnNode("#repeatButton");

        verify(mockPlaylistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatOne() {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ONE);

        clickOnNode("#repeatButton");

        verify(mockPlaylistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_ONE + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatAll() {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ALL);

        clickOnNode("#repeatButton");

        verify(mockPlaylistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_ALL + "')");
    }

    @Test
    public void shouldClickEqButton() {
        clickOnNode("#eqButton");

        verify(mockEqualizerController, times(1)).updateSliderValues();
        verify(mockEqualizerView, times(1)).show(true);
    }

    @Test
    @SneakyThrows
    public void shouldClickRandomButtonWithYearFilter() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(0));

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = (List<Track>) mock(List.class);

        when(mockSearchManager.getShuffledPlaylist(anyInt(), anyString())).thenReturn(mockTracks);

        clickOnNode("#randomButton");

        verify(mockSearchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), "2000");
        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, mockTracks);
        verify(mockPlaylistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldClickRandomButtonWithNoYearFilter() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().clearSelection());

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = (List<Track>) mock(List.class);

        when(mockSearchManager.getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null)).thenReturn(mockTracks);

        clickOnNode("#randomButton");

        verify(mockSearchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null);
        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, mockTracks);
        verify(mockPlaylistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldFirePlaylistSelected() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites Playlist", 10));
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playlistPanelListView.getItems()).hasSize(2);

        threadRunner.runOnGui(() -> playlistPanelListView.getSelectionModel().select(1));

        WaitForAsyncUtils.waitForFxEvents();

        verify(getMockEventManager(), times(1)).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveApplicationInitialised() {
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

        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
                new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        when(mockPlaylistManager.getPlaylists()).thenReturn(playlists);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(null);

        threadRunner.runOnGui(() -> mainPanelController.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(1);
        assertThat(yearFilter.getYear()).isNull();

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(mainPanelController, "observablePlaylists");
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
    @SneakyThrows
    public void shouldReceiveApplicationInitialisedWithEmptyYearList() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
                new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        when(mockPlaylistManager.getPlaylists()).thenReturn(playlists);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(Collections.emptyList());

        threadRunner.runOnGui(() -> mainPanelController.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(1);
        assertThat(yearFilter.getYear()).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveApplicationInitialisedWithYearList() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
                new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        when(mockPlaylistManager.getPlaylists()).thenReturn(playlists);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(Arrays.asList("2000", "2001"));

        threadRunner.runOnGui(() -> mainPanelController.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(3);
        assertThat(yearFilter.getYear()).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveApplicationInitialisedWithNoPlaylists() {
        when(mockPlaylistManager.getPlaylists()).thenReturn(Collections.emptyList());
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(null);

        threadRunner.runOnGui(() -> mainPanelController.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(mainPanelController, "observablePlaylists");
        assertThat(observablePlaylists).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveDataIndexed() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        threadRunner.runOnGui(() -> spyMainPanelController.eventReceived(Event.DATA_INDEXED));

        WaitForAsyncUtils.waitForFxEvents();

        verify(spyMainPanelController, times(1)).updateYearFilter();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveNewUpdateAvailable() {
        Button newVersionButton = find("#newVersionButton");
        Version version = new Version("99.99.99");

        threadRunner.runOnGui(() -> {
            newVersionButton.setText(null);
            newVersionButton.setDisable(true);
            newVersionButton.setVisible(false);

            mainPanelController.eventReceived(Event.NEW_VERSION_AVAILABLE, version);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(newVersionButton.getText()).isEqualTo(messageManager.getMessage(MESSAGE_NEW_VERSION_AVAILABLE, version));
        assertThat(newVersionButton.isDisabled()).isFalse();
        assertThat(newVersionButton.isVisible()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMuteUpdated() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        threadRunner.runOnGui(() -> spyMainPanelController.eventReceived(Event.MUTE_UPDATED));

        WaitForAsyncUtils.waitForFxEvents();

        verify(spyMainPanelController, times(1)).setVolumeButtonImage();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTimeUpdated() {
        Duration mediaDuration = new Duration(30000);
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            timeSlider.setDisable(true);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.isDisabled()).isFalse();
        assertThat(timeSlider.getSliderValue()).isEqualTo(50.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15/00:30");
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTimeUpdatedMediaDurationUnknown() {
        Duration mediaDuration = Duration.UNKNOWN;
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            timeSlider.setDisable(false);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.isDisabled()).isTrue();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15");
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTimeUpdatedZeroMediaDuration() {
        Duration mediaDuration = Duration.ZERO;
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            timeSlider.setDisable(false);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.isDisabled()).isFalse();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15");
    }

    @Test
    @SneakyThrows
    public void shouldReceiveBufferUpdated() {
        Duration mediaDuration = new Duration(30000);
        Duration bufferProgressTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");

        threadRunner.runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, mediaDuration, bufferProgressTime);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.5d);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveBufferUpdatedWithNullMediaDuration() {
        Duration bufferProgressTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");

        threadRunner.runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, null, bufferProgressTime);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveBufferUpdatedWithNullBufferProgressTime() {
        Duration mediaDuration = new Duration(30000);
        SliderProgressBar timeSlider = find("#timeSlider");

        threadRunner.runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, mediaDuration, null);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaPlaying() {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");

        threadRunner.runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(true);
            previousButton.setDisable(true);
            nextButton.setDisable(true);

            mainPanelController.eventReceived(Event.MEDIA_PLAYING);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PAUSE + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isFalse();
        assertThat(nextButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaPaused() {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");

        threadRunner.runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(true);
            previousButton.setDisable(false);
            nextButton.setDisable(false);

            mainPanelController.eventReceived(Event.MEDIA_PAUSED);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isTrue();
        assertThat(nextButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaStopped() {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.MEDIA_STOPPED);
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
    @SneakyThrows
    public void shouldReceiveEndOfMedia() {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);

        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.END_OF_MEDIA);
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
    @SneakyThrows
    public void shouldReceiveEndOfMediaWithRepeatOne() {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ONE);

        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        threadRunner.runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.END_OF_MEDIA);
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
    @SneakyThrows
    public void shouldReceivePlaylistSelected() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedWithNullPayload() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, (Object[]) null);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedWithEmptyPayload() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedExistingPlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedPlaylistIsNotEmpty() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        favourites.addTrack(mock(Track.class));
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(true);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistDeleted() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_DELETED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWithEdit() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, true);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWithoutEdit() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, false);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackSelected() {
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(true);

            mainPanelController.eventReceived(Event.TRACK_SELECTED);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlaying() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        String albumImageUrl = "http://www.example.com/image.png";
        Image albumImage = new Image(albumImageUrl);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn(albumImageUrl);
        doAnswer(invocation -> {
            ImageView imageView = invocation.getArgument(0);
            imageView.setImage(albumImage);

            return null;
        }).when(mockImageFactory).loadImage(playingImageView, albumImageUrl);

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
        assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
        assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
        assertThat(playingImageView.getImage()).isNotNull();
        assertThat(playPauseButton.isDisabled()).isTrue();

        verify(mockNativeManager, times(1)).displayNotification(track);
        verify(mockImageFactory, times(1)).loadImage(playingImageView, albumImageUrl);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlayingNullPayload() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn("http://www.example.com/image.png");

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, (Object[]) null);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(mockNativeManager, never()).displayNotification(track);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlayingEmptyPayload() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn("http://www.example.com/image.png");

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(mockNativeManager, never()).displayNotification(track);
    }

    @Test
    @SneakyThrows
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithBackSpace() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        playlistPanelListView.onKeyPressedProperty().get()
                .handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.BACK_SPACE));

        // Wait for the UI thread
        Thread.sleep(250);

        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mockConfirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(mockConfirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(mockConfirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(mockPlaylistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
    }

    @Test
    @SneakyThrows
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithDelete() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        playlistPanelListView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.DELETE));

        // Wait for the UI thread
        Thread.sleep(250);

        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mockConfirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(mockConfirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(mockConfirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(mockPlaylistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
    }

    @Test
    @SneakyThrows
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithUnknownKey() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        playlistPanelListView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.A));

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    public void shouldReceiveMenuFileImportPlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleImportPlaylistButtonAction();

        spyMainPanelController.eventReceived(MENU_FILE_IMPORT_PLAYLIST);

        verify(spyMainPanelController, times(1)).handleImportPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuFileExportPlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleExportPlaylistButtonAction();

        spyMainPanelController.eventReceived(MENU_FILE_EXPORT_PLAYLIST);

        verify(spyMainPanelController, times(1)).handleExportPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuFileSettings() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleSettingsButtonAction();

        spyMainPanelController.eventReceived(MENU_FILE_SETTINGS);

        verify(spyMainPanelController, times(1)).handleSettingsButtonAction();
    }

    @Test
    public void shouldReceiveMenuEditAddPlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleAddPlaylistButtonAction();

        spyMainPanelController.eventReceived(MENU_EDIT_ADD_PLAYLIST);

        verify(spyMainPanelController, times(1)).handleAddPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuEditDeletePlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleDeletePlaylistButtonAction();

        spyMainPanelController.eventReceived(MENU_EDIT_DELETE_PLAYLIST);

        verify(spyMainPanelController, times(1)).handleDeletePlaylistButtonAction();
    }

    @Test
    public void shouldReceiveEditCreatePlaylistFromAlbumWithSelectedTrack() {
        Track mockTrack = mock(Track.class);

        when(mockTrackTableController.getSelectedTrack()).thenReturn(mockTrack);

        mainPanelController.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

        verify(mockPlaylistManager, times(1)).createPlaylistFromAlbum(mockTrack);
    }

    @Test
    public void shouldReceiveEditCreatePlaylistFromAlbumWithoutSelectedTrack() {
        when(mockTrackTableController.getSelectedTrack()).thenReturn(null);

        mainPanelController.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

        verify(mockPlaylistManager, never()).createPlaylistFromAlbum(any());
    }

    @Test
    public void shouldReceiveMenuEditRandomPlaylist() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleRandomButtonAction();

        spyMainPanelController.eventReceived(MENU_EDIT_RANDOM_PLAYLIST);

        verify(spyMainPanelController, times(1)).handleRandomButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsPlayPause() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handlePlayPauseButtonAction();

        spyMainPanelController.eventReceived(MENU_CONTROLS_PLAY_PAUSE);

        verify(spyMainPanelController, times(1)).handlePlayPauseButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsPrevious() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handlePreviousButtonAction();

        spyMainPanelController.eventReceived(MENU_CONTROLS_PREVIOUS);

        verify(spyMainPanelController, times(1)).handlePreviousButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsNext() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleNextButtonAction();

        spyMainPanelController.eventReceived(MENU_CONTROLS_NEXT);

        verify(spyMainPanelController, times(1)).handleNextButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsShuffle() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).setShuffleButtonImage();

        spyMainPanelController.eventReceived(MENU_CONTROLS_SHUFFLE);

        verify(spyMainPanelController, times(1)).setShuffleButtonImage();
    }

    @Test
    public void shouldReceiveMenuControlsRepeat() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).setRepeatButtonImage();

        spyMainPanelController.eventReceived(MENU_CONTROLS_REPEAT);

        verify(spyMainPanelController, times(1)).setRepeatButtonImage();
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(20d);
        verify(mockMediaManager, times(1)).setVolumePercent(20d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithPayloadOver100() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(95d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(100d);
        verify(mockMediaManager, times(1)).setVolumePercent(100d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithoutPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_UP);

        assertThat(volumeSlider.getValue()).isEqualTo(10d);
        verify(mockMediaManager, never()).setVolumePercent(anyDouble());
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(90d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(80d);
        verify(mockMediaManager, times(1)).setVolumePercent(80d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithPayloadBelowZero() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(5d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(0d);
        verify(mockMediaManager, times(1)).setVolumePercent(0d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithoutPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        mainPanelController.eventReceived(MENU_CONTROLS_VOLUME_DOWN);

        assertThat(volumeSlider.getValue()).isEqualTo(10d);
        verify(mockMediaManager, never()).setVolumePercent(anyDouble());
    }

    @Test
    public void shouldReceiveMenuControlsVolumeMute() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleVolumeButtonAction();

        spyMainPanelController.eventReceived(MENU_CONTROLS_VOLUME_MUTE);

        verify(spyMainPanelController, times(1)).handleVolumeButtonAction();
    }

    @Test
    public void shouldReceiveMenuViewEqualizer() {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        doNothing().when(spyMainPanelController).handleEqButtonAction();

        spyMainPanelController.eventReceived(MENU_VIEW_EQUALIZER);

        verify(spyMainPanelController, times(1)).handleEqButtonAction();
    }

    @After
    public void cleanup() {
        setField(GUIState.class, "stage", existingStage);
    }
}
