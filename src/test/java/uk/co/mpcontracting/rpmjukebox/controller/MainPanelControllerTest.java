package uk.co.mpcontracting.rpmjukebox.controller;

import com.google.gson.Gson;
import com.igormaznitsa.commons.version.Version;
import de.felixroske.jfxsupport.GUIState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.*;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.support.ThreadRunner.runOnGui;

public class MainPanelControllerTest extends AbstractTest implements Constants {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private MainPanelController mainPanelController;

    @Autowired
    private MainPanelView mainPanelView;

    @Autowired
    private MessageManager messageManager;

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
    private Stage mockStage;
    private Scene mockScene;
    private Parent mockRoot;

    @PostConstruct
    public void constructView() throws Exception {
        init(mainPanelView);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        existingStage = GUIState.getStage();

        mockStage = mock(Stage.class);
        mockScene = mock(Scene.class);
        mockRoot = mock(Parent.class);
        when(mockStage.getScene()).thenReturn(mockScene);
        when(mockScene.getRoot()).thenReturn(mockRoot);
        setField(GUIState.class, "stage", mockStage);

        setField(mainPanelController, "eventManager", getMockEventManager());
        setField(mainPanelController, "equalizerView", mockEqualizerView);
        setField(mainPanelController, "settingsView", mockSettingsView);
        setField(mainPanelController, "exportView", mockExportView);
        setField(mainPanelController, "messageView", mockMessageView);
        setField(mainPanelController, "confirmView", mockConfirmView);
        setField(mainPanelController, "trackTableView", mockTrackTableView);
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

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            ((TextField) getField(mainPanelController, "searchTextField")).setText(null);
            ((ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox")).getItems()
                .clear();
            ((ListView<Playlist>) getField(mainPanelController, "playlistPanelListView")).getItems()
                .clear();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        reset(mockSearchManager);
        reset(mockPlaylistManager);
    }

    @Test
    public void shouldShowMessageView() throws Exception {
        mainPanelController.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).setMessage("Message");
        verify(mockMessageView, times(1)).show(anyBoolean());
    }

    @Test
    public void shouldShowMessageViewAlreadyShowing() throws Exception {
        when(mockMessageView.isShowing()).thenReturn(true);

        mainPanelController.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).setMessage("Message");
        verify(mockMessageView, never()).show(anyBoolean());
    }

    @Test
    public void shouldCloseMessageView() throws Exception {
        mainPanelController.closeMessageView();

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockMessageView, times(1)).close();
    }

    @Test
    public void shouldShowConfirmView() throws Exception {
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
    public void shouldShowConfirmViewAlreadyShowing() throws Exception {
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
    public void shouldUpdateSearchTextSearchCriteria() throws Exception {
        TextField searchTextField = (TextField) getField(mainPanelController, "searchTextField");
        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            searchTextField.setText("Search");
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        TrackSearch trackSearch = new TrackSearch("Search");

        verify(mockSearchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldUpdateYearFilterSearchCriteria() throws Exception {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(1);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, Collections.emptyList());
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldUpdateYearFilterAndSearchTextSearchCriteria() throws Exception {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        TextField searchTextField = (TextField) getField(mainPanelController, "searchTextField");
        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(1);

            reset(mockPlaylistManager);

            searchTextField.setText("Search");
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        TrackSearch trackSearch = new TrackSearch("Search", new TrackFilter(null, "2001"));

        verify(mockSearchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldUpdatePlayingPlaylistOnYearFilterUpdate() throws Exception {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = mock(List.class);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);
        when(mockPlaylist.getTracks()).thenReturn(mockTracks);
        when(mockPlaylistManager.getPlayingPlaylist()).thenReturn(mockPlaylist);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(1);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldClickDeletePlaylistButton() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldClickDeletePlaylistButtonWithReservedPlaylist() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(PLAYLIST_ID_SEARCH, "Playlist 1", 10);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        clickOnNode("#deletePlaylistButton");

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    public void shouldClickDeletePlaylistButtonWithNullPlaylist() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getSelectionModel().clearSelection();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        clickOnNode("#deletePlaylistButton");

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @Test
    public void shouldClickImportPlaylistButton() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch1 = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
            latch1.countDown();
        });

        latch1.await(2000, TimeUnit.MILLISECONDS);

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
            when(mockSearchManager.getTrackById(Integer.toString(i))).thenReturn(mockTrack);
        }

        List<PlaylistSettings> playlistSettings = new ArrayList<>();
        playlistSettings.add(new PlaylistSettings(playlist));

        when(mockGson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(playlistSettings);

        CountDownLatch latch2 = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.handleImportPlaylistButtonAction(new ActionEvent());
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldClickImportPlaylistButtonWithNullTracksFromSearch() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
        when(mockSearchManager.getTrackById(any())).thenReturn(null);

        CountDownLatch latch2 = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.handleImportPlaylistButtonAction(new ActionEvent());
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldClickImportPlaylistButtonWithNullPlaylistSettings() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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

        CountDownLatch latch2 = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.handleImportPlaylistButtonAction(new ActionEvent());
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

        verify(mockPlaylistManager, never()).addPlaylist(any());
        verify(mockPlaylistManager, never()).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    public void shouldClickImportPlaylistButtonWithNullFile() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
        when(mockFileChooser.showOpenDialog(any())).thenReturn(null);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        CountDownLatch latch2 = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.handleImportPlaylistButtonAction(new ActionEvent());
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

        verify(mockPlaylistManager, never()).addPlaylist(any());
        verify(mockPlaylistManager, never()).getPlaylists();
        verify(mockRoot, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(mockRoot, times(1)).setEffect(null);
    }

    @Test
    public void shouldClickImportPlaylistButtonWhenExceptionThrown() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        FileChooser mockFileChooser = mock(FileChooser.class);
        when(mockFileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());

        File mockFile = mock(File.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(mockFile);
        doReturn(mockFileChooser).when(spyMainPanelController).constructFileChooser();

        doThrow(new RuntimeException("MainPanelControllerTest.shouldClickImportPlaylistButtonWhenExceptionThrown()"))
            .when(spyMainPanelController).constructFileReader(any());

        CountDownLatch latch2 = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.handleImportPlaylistButtonAction(new ActionEvent());
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

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
        when(mockMediaManager.getPlayingTimeSeconds()).thenReturn((double)appProperties.getPreviousSecondsCutoff());

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
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(mockPlaylist);

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
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(mockPlaylist);
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
        when(mockPlaylistManager.getPlaylist(anyInt())).thenReturn(mockPlaylist);
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
    public void shouldClickRandomButtonWithYearFilter() throws Exception {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(0);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = (List<Track>)mock(List.class);

        when(mockSearchManager.getShuffledPlaylist(anyInt(), anyString())).thenReturn(mockTracks);

        clickOnNode("#randomButton");

        verify(mockSearchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), "2000");
        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, mockTracks);
        verify(mockPlaylistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldClickRandomButtonWithNoYearFilter() throws Exception {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().clearSelection();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        List<Track> mockTracks = (List<Track>)mock(List.class);

        when(mockSearchManager.getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null)).thenReturn(mockTracks);

        clickOnNode("#randomButton");

        verify(mockSearchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null);
        verify(mockPlaylistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, mockTracks);
        verify(mockPlaylistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldReceiveApplicationInitialised() throws Exception {
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

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            mainPanelController.eventReceived(Event.APPLICATION_INITIALISED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldReceiveApplicationInitialisedWithEmptyYearList() throws Exception {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        when(mockPlaylistManager.getPlaylists()).thenReturn(playlists);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(Collections.emptyList());

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            mainPanelController.eventReceived(Event.APPLICATION_INITIALISED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(1);
        assertThat(yearFilter.getYear()).isNull();
    }

    @Test
    public void shouldReceiveApplicationInitialisedWithYearList() throws Exception {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        when(mockPlaylistManager.getPlaylists()).thenReturn(playlists);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(Arrays.asList("2000", "2001"));

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            mainPanelController.eventReceived(Event.APPLICATION_INITIALISED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(3);
        assertThat(yearFilter.getYear()).isNull();
    }

    @Test
    public void shouldReceiveApplicationInitialisedWithNoPlaylists() throws Exception {
        when(mockPlaylistManager.getPlaylists()).thenReturn(Collections.emptyList());
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(mockPlaylistManager.isShuffle()).thenReturn(false);
        when(mockMediaManager.isMuted()).thenReturn(false);
        when(mockSearchManager.getYearList()).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            mainPanelController.eventReceived(Event.APPLICATION_INITIALISED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(mainPanelController, "observablePlaylists");
        assertThat(observablePlaylists).isEmpty();
    }

    @Test
    public void shouldReceiveDataIndexed() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.eventReceived(Event.DATA_INDEXED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        verify(spyMainPanelController, times(1)).updateYearFilter();
    }

    @Test
    public void shouldReceiveNewUpdateAvailable() throws Exception {
        Button newVersionButton = find("#newVersionButton");
        Version version = new Version("99.99.99");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            newVersionButton.setText(null);
            newVersionButton.setDisable(true);
            newVersionButton.setVisible(false);

            mainPanelController.eventReceived(Event.NEW_VERSION_AVAILABLE, version);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(newVersionButton.getText()).isEqualTo(messageManager.getMessage(MESSAGE_NEW_VERSION_AVAILABLE, version));
        assertThat(newVersionButton.isDisabled()).isFalse();
        assertThat(newVersionButton.isVisible()).isTrue();
    }

    @Test
    public void shouldReceiveMuteUpdated() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            spyMainPanelController.eventReceived(Event.MUTE_UPDATED);
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        verify(spyMainPanelController, times(1)).setVolumeButtonImage();
    }

    @Test
    public void shouldReceiveTimeUpdated() throws Exception {
        Duration mediaDuration = new Duration(30000);
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setDisable(true);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.isDisabled()).isFalse();
        assertThat(timeSlider.getSliderValue()).isEqualTo(50.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15/00:30");
    }

    @Test
    public void shouldReceiveTimeUpdatedMediaDurationUnknown() throws Exception {
        Duration mediaDuration = Duration.UNKNOWN;
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setDisable(false);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.isDisabled()).isTrue();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15");
    }

    @Test
    public void shouldReceiveTimeUpdatedZeroMediaDuration() throws Exception {
        Duration mediaDuration = Duration.ZERO;
        Duration currentTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setDisable(false);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.isDisabled()).isFalse();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:15");
    }

    @Test
    public void shouldReceiveBufferUpdated() throws Exception {
        Duration mediaDuration = new Duration(30000);
        Duration bufferProgressTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, mediaDuration, bufferProgressTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.5d);
    }

    @Test
    public void shouldReceiveBufferUpdatedWithNullMediaDuration() throws Exception {
        Duration mediaDuration = null;
        Duration bufferProgressTime = new Duration(15000);
        SliderProgressBar timeSlider = find("#timeSlider");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, mediaDuration, bufferProgressTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldReceiveBufferUpdatedWithNullBufferProgressTime() throws Exception {
        Duration mediaDuration = new Duration(30000);
        Duration bufferProgressTime = null;
        SliderProgressBar timeSlider = find("#timeSlider");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            timeSlider.setProgressValue(0);

            mainPanelController.eventReceived(Event.BUFFER_UPDATED, mediaDuration, bufferProgressTime);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldReceiveMediaPlaying() throws Exception {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(true);
            previousButton.setDisable(true);
            nextButton.setDisable(true);

            mainPanelController.eventReceived(Event.MEDIA_PLAYING);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PAUSE + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isFalse();
        assertThat(nextButton.isDisabled()).isFalse();
    }

    @Test
    public void shouldReceiveMediaPaused() throws Exception {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(true);
            previousButton.setDisable(false);
            nextButton.setDisable(false);

            mainPanelController.eventReceived(Event.MEDIA_PAUSED);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isTrue();
        assertThat(nextButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceiveMediaStopped() throws Exception {
        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.MEDIA_STOPPED);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isTrue();
        assertThat(nextButton.isDisabled()).isTrue();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
    }

    @Test
    public void shouldReceiveEndOfMedia() throws Exception {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.OFF);

        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.END_OF_MEDIA);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isTrue();
        assertThat(nextButton.isDisabled()).isTrue();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(timeSlider.getProgressValue()).isEqualTo(0.0d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
    }

    @Test
    public void shouldReceiveEndOfMediaWithRepeatOne() throws Exception {
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ONE);

        Button playPauseButton = find("#playPauseButton");
        Button previousButton = find("#previousButton");
        Button nextButton = find("#nextButton");
        SliderProgressBar timeSlider = find("#timeSlider");
        Label playTimeLabel = find("#playTimeLabel");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setStyle(null);
            playPauseButton.setDisable(false);
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            timeSlider.setSliderValue(99);
            timeSlider.setProgressValue(99);
            playTimeLabel.setText(null);

            mainPanelController.eventReceived(Event.END_OF_MEDIA);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_PLAY + "')");
        assertThat(playPauseButton.isDisabled()).isFalse();
        assertThat(previousButton.isDisabled()).isTrue();
        assertThat(nextButton.isDisabled()).isTrue();
        assertThat(timeSlider.getSliderValue()).isEqualTo(0.0d);
        assertThat(timeSlider.getProgressValue()).isEqualTo(0.99d);
        assertThat(playTimeLabel.getText()).isEqualTo("00:00/00:00");
    }

    @Test
    public void shouldReceivePlaylistSelected() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceivePlaylistSelectedWithNullPayload() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, (Object[])null);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    public void shouldReceivePlaylistSelectedWithEmptyPayload() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, new Object[] {});

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    public void shouldReceivePlaylistSelectedExistingPlaylist() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceivePlaylistSelectedPlaylistIsNotEmpty() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        favourites.addTrack(mock(Track.class));
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(true);

            spyMainPanelController.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, never()).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    public void shouldReceivePlaylistDeleted() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_DELETED, PLAYLIST_ID_FAVOURITES);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceivePlaylistCreatedWithEdit() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, true);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceivePlaylistCreatedWithoutEdit() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        setField(spyMainPanelController, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(mockPlaylistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(search);
        when(mockPlaylistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(favourites);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        when(mockMediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(spyMainPanelController, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            spyMainPanelController.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, false);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        int currentSelectedPlaylistId = (Integer) getField(spyMainPanelController, "currentSelectedPlaylistId");

        verify(spyMainPanelController, times(1)).updateObservablePlaylists();
        verify(mockPlaylistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    public void shouldReceiveTrackSelected() throws Exception {
        Button playPauseButton = find("#playPauseButton");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(true);

            mainPanelController.eventReceived(Event.TRACK_SELECTED);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    public void shouldReceiveTrackQueuedForPlaying() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789",
            "Track Name", 1, "Location", true, null);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
            .thenReturn("http://www.example.com/image.png");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
        assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
        assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
        assertThat(playingImageView.getImage()).isNotNull();
        assertThat(playPauseButton.isDisabled()).isTrue();

        verify(mockNativeManager, times(1)).displayNotification(track);
    }

    @Test
    public void shouldReceiveTrackQueuedForPlayingNoAlbumImage() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", null, 2000, "789",
            "Track Name", 1, "Location", true, null);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
            .thenReturn("http://www.example.com/image.png");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
        assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
        assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
        assertThat(playingImageView.getImage()).isNotNull();
        assertThat(playPauseButton.isDisabled()).isTrue();

        verify(mockNativeManager, times(1)).displayNotification(track);
    }

    @Test
    public void shouldReceiveTrackQueuedForPlayingNoImages() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = new Track("123", "Artist Name", null, "456", "Album Name", null, 2000, "789", "Track Name", 1,
            "Location", true, null);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
            .thenReturn("http://www.example.com/image.png");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
        assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
        assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
        assertThat(playingImageView.getImage()).isNotNull();
        assertThat(playPauseButton.isDisabled()).isTrue();

        verify(mockNativeManager, times(1)).displayNotification(track);
    }

    @Test
    public void shouldReceiveTrackQueuedForPlayingNullPayload() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = new Track("123", "Artist Name", null, "456", "Album Name", null, 2000, "789", "Track Name", 1,
            "Location", true, null);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
            .thenReturn("http://www.example.com/image.png");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, (Object[])null);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(mockNativeManager, never()).displayNotification(track);
    }

    @Test
    public void shouldReceiveTrackQueuedForPlayingEmptyPayload() throws Exception {
        MainPanelController spyMainPanelController = spy(mainPanelController);

        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = new Track("123", "Artist Name", null, "456", "Album Name", null, 2000, "789", "Track Name", 1,
            "Location", true, null);

        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString()))
            .thenReturn("http://www.example.com/image.png");

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            spyMainPanelController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, new Object[] {});

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(mockNativeManager, never()).displayNotification(track);
    }

    @Test
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithBackSpace() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithDelete() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Playlist playlist = new Playlist(1, "Playlist 1", 10);

        CountDownLatch latch = new CountDownLatch(1);

        runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);

            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

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
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithUnknownKey() throws Exception {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        playlistPanelListView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.A));

        // Wait for the UI thread
        Thread.sleep(250);

        verify(mockConfirmView, never()).show(anyBoolean());
    }

    @After
    public void cleanup() {
        setField(GUIState.class, "stage", existingStage);
    }
}
