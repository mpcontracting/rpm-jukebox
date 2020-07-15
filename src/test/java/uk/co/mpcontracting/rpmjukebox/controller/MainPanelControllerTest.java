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
    private ImageFactory imageFactory;

    @Mock
    private EqualizerView equalizerView;

    @Mock
    private SettingsView settingsView;

    @Mock
    private ExportView exportView;

    @Mock
    private MessageView messageView;

    @Mock
    private ConfirmView confirmView;

    @Mock
    private TrackTableView trackTableView;

    @Mock
    private TrackTableController trackTableController;

    @Mock
    private EqualizerController equalizerController;

    @Mock
    private SettingsController settingsController;

    @Mock
    private ExportController exportController;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private SearchManager searchManager;

    @Mock
    private PlaylistManager playlistManager;

    @Mock
    private MediaManager mediaManager;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private NativeManager nativeManager;

    @Mock
    private UpdateManager updateManager;

    private MainPanelController underTest;
    private Stage existingStage;
    private Parent root;

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

        Stage stage = mock(Stage.class);
        Scene scene = mock(Scene.class);
        root = mock(Parent.class);
        when(stage.getScene()).thenReturn(scene);
        when(scene.getRoot()).thenReturn(root);
        setField(GUIState.class, "stage", stage);

        setField(mainPanelController, "imageFactory", imageFactory);
        setField(mainPanelController, "eventManager", getMockEventManager());
        setField(mainPanelController, "equalizerView", equalizerView);
        setField(mainPanelController, "settingsView", settingsView);
        setField(mainPanelController, "exportView", exportView);
        setField(mainPanelController, "messageView", messageView);
        setField(mainPanelController, "confirmView", confirmView);
        setField(mainPanelController, "trackTableView", trackTableView);
        setField(mainPanelController, "trackTableController", trackTableController);
        setField(mainPanelController, "equalizerController", equalizerController);
        setField(mainPanelController, "settingsController", settingsController);
        setField(mainPanelController, "exportController", exportController);
        setField(mainPanelController, "settingsManager", settingsManager);
        setField(mainPanelController, "searchManager", searchManager);
        setField(mainPanelController, "playlistManager", playlistManager);
        setField(mainPanelController, "mediaManager", mediaManager);
        setField(mainPanelController, "cacheManager", cacheManager);
        setField(mainPanelController, "nativeManager", nativeManager);
        setField(mainPanelController, "updateManager", updateManager);

        threadRunner.runOnGui(() -> {
            ((TextField) getNonNullField(mainPanelController, "searchTextField")).setText(null);
            ((ComboBox<YearFilter>) getNonNullField(mainPanelController, "yearFilterComboBox")).getItems().clear();
            ((ListView<Playlist>) getNonNullField(mainPanelController, "playlistPanelListView")).getItems().clear();
        });

        WaitForAsyncUtils.waitForFxEvents();

        reset(searchManager);
        reset(playlistManager);
        reset(getMockEventManager());

        underTest = spy(mainPanelController);
    }

    @Test
    @SneakyThrows
    public void shouldShowMessageView() {
        underTest.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(messageView, times(1)).setMessage("Message");
        verify(messageView, times(1)).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldShowMessageViewAlreadyShowing() {
        when(messageView.isShowing()).thenReturn(true);

        underTest.showMessageView("Message", true);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(messageView, times(1)).setMessage("Message");
        verify(messageView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldCloseMessageView() {
        underTest.closeMessageView();

        // Wait for the UI thread
        Thread.sleep(250);

        verify(messageView, times(1)).close();
    }

    @Test
    @SneakyThrows
    public void shouldShowConfirmView() {
        Runnable okRunnable = mock(Runnable.class);
        Runnable cancelRunnable = mock(Runnable.class);

        underTest.showConfirmView("Message", true, okRunnable, cancelRunnable);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(confirmView, times(1)).setMessage("Message");
        verify(confirmView, times(1)).setRunnables(okRunnable, cancelRunnable);
        verify(confirmView, times(1)).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldShowConfirmViewAlreadyShowing() {
        when(confirmView.isShowing()).thenReturn(true);

        Runnable okRunnable = mock(Runnable.class);
        Runnable cancelRunnable = mock(Runnable.class);

        underTest.showConfirmView("Message", true, okRunnable, cancelRunnable);

        // Wait for the UI thread
        Thread.sleep(250);

        verify(confirmView, times(1)).setMessage("Message");
        verify(confirmView, times(1)).setRunnables(okRunnable, cancelRunnable);
        verify(confirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldUpdateSearchTextSearchCriteria() {
        TextField searchTextField = (TextField) getNonNullField(underTest, "searchTextField");

        threadRunner.runOnGui(() -> searchTextField.setText("Search"));

        WaitForAsyncUtils.waitForFxEvents();

        TrackSearch trackSearch = new TrackSearch("Search");

        verify(searchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdateYearFilterSearchCriteria() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(underTest, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(1));

        WaitForAsyncUtils.waitForFxEvents();

        verify(playlistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, Collections.emptyList());
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdateYearFilterAndSearchTextSearchCriteria() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(underTest, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        TextField searchTextField = (TextField) getNonNullField(underTest, "searchTextField");

        threadRunner.runOnGui(() -> {
            yearFilterComboBox.getSelectionModel().select(1);

            reset(playlistManager);

            searchTextField.setText("Search");
        });

        WaitForAsyncUtils.waitForFxEvents();

        TrackSearch trackSearch = new TrackSearch("Search", new TrackFilter(null, "2001"));

        verify(searchManager, times(1)).search(trackSearch);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldUpdatePlayingPlaylistOnYearFilterUpdate() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(underTest, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));

        @SuppressWarnings("unchecked")
        List<Track> tracks = mock(List.class);

        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);
        when(playlist.getTracks()).thenReturn(tracks);
        when(playlistManager.getPlayingPlaylist()).thenReturn(playlist);

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(1));

        WaitForAsyncUtils.waitForFxEvents();

        verify(playlistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
        verify(playlist, times(1)).getTracks();
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldClickNewVersionButton() {
        clickOnNode("#newVersionButton");

        verify(updateManager, times(1)).downloadNewVersion();
    }

    @Test
    public void shouldClickAddPlaylistButton() {
        clickOnNode("#addPlaylistButton");

        verify(playlistManager, times(1)).createPlaylist();
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

        verify(confirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(confirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(confirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(playlistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
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

        verify(confirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldClickDeletePlaylistButtonWithNullPlaylist() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> playlistPanelListView.getSelectionModel().clearSelection());

        WaitForAsyncUtils.waitForFxEvents();

        clickOnNode("#deletePlaylistButton");

        verify(confirmView, never()).show(anyBoolean());
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButton() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
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
        when(settingsManager.getGson()).thenReturn(gson);

        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            Track track = mock(Track.class);
            when(track.getTrackId()).thenReturn(Integer.toString(i));

            playlist.addTrack(track);
            when(searchManager.getTrackById(Integer.toString(i))).thenReturn(of(track));
        }

        List<PlaylistSettings> playlistSettings = new ArrayList<>();
        playlistSettings.add(new PlaylistSettings(playlist));

        when(gson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(playlistSettings);

        threadRunner.runOnGui(underTest::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

        verify(playlistManager, times(1)).addPlaylist(playlistCaptor.capture());

        Playlist result = playlistCaptor.getValue();

        assertThat(result).isEqualTo(playlist);
        assertThat(result.getTracks()).hasSize(playlist.getTracks().size());

        verify(playlistManager, times(1)).getPlaylists();
        verify(root, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(root, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullTracksFromSearch() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
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
        when(settingsManager.getGson()).thenReturn(gson);

        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            Track track = mock(Track.class);
            when(track.getTrackId()).thenReturn(Integer.toString(i));

            playlist.addTrack(track);
        }

        List<PlaylistSettings> playlistSettings = new ArrayList<>();
        playlistSettings.add(new PlaylistSettings(playlist));

        when(gson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(playlistSettings);
        when(searchManager.getTrackById(any())).thenReturn(empty());

        threadRunner.runOnGui(underTest::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

        verify(playlistManager, times(1)).addPlaylist(playlistCaptor.capture());

        Playlist result = playlistCaptor.getValue();

        assertThat(result).isEqualTo(playlist);
        assertThat(result.getTracks()).isEmpty();

        verify(playlistManager, times(1)).getPlaylists();
        verify(root, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(root, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullPlaylistSettings() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
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
        when(settingsManager.getGson()).thenReturn(gson);
        when(gson.fromJson(Mockito.any(FileReader.class), Mockito.any(Type.class))).thenReturn(null);

        threadRunner.runOnGui(underTest::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(playlistManager, never()).addPlaylist(any());
        verify(playlistManager, never()).getPlaylists();
        verify(root, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(root, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWithNullFile() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
            playlistPanelListView.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();

        FileChooser fileChooser = mock(FileChooser.class);
        when(fileChooser.getExtensionFilters()).thenReturn(FXCollections.observableArrayList());
        when(fileChooser.showOpenDialog(any())).thenReturn(null);
        doReturn(fileChooser).when(underTest).constructFileChooser();

        threadRunner.runOnGui(underTest::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(playlistManager, never()).addPlaylist(any());
        verify(playlistManager, never()).getPlaylists();
        verify(root, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(root, times(1)).setEffect(null);
    }

    @Test
    @SneakyThrows
    public void shouldClickImportPlaylistButtonWhenExceptionThrown() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        threadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(new Playlist(PLAYLIST_ID_SEARCH, "Search Playlist", 10));
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

        threadRunner.runOnGui(underTest::handleImportPlaylistButtonAction);

        WaitForAsyncUtils.waitForFxEvents();

        verify(playlistManager, never()).addPlaylist(any());
        verify(playlistManager, never()).getPlaylists();
        verify(root, times(1)).setEffect(Mockito.any(BoxBlur.class));
        verify(root, times(1)).setEffect(null);
    }

    @Test
    public void shouldClickExportPlaylistButton() {
        clickOnNode("#exportPlaylistButton");

        verify(exportController, times(1)).bindPlaylists();
        verify(exportView, times(1)).show(true);
    }

    @Test
    public void shouldClickSettingsButton() {
        clickOnNode("#settingsButton");

        verify(settingsController, times(1)).bindSystemSettings();
        verify(settingsView, times(1)).show(true);
    }

    @Test
    public void shouldClickPreviousButton() {
        clickOnNode("#previousButton");

        verify(mediaManager, never()).setSeekPositionPercent(0d);
        verify(playlistManager, times(1)).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPreviousButtonWhenPlayingLessThanEqualCutoff() {
        when(mediaManager.getPlayingTimeSeconds()).thenReturn((double) appProperties.getPreviousSecondsCutoff());

        clickOnNode("#previousButton");

        verify(mediaManager, never()).setSeekPositionPercent(0d);
        verify(playlistManager, times(1)).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPreviousButtonWhenPlayingGreaterThanCutoff() {
        when(mediaManager.getPlayingTimeSeconds()).thenReturn(appProperties.getPreviousSecondsCutoff() + 1d);

        clickOnNode("#previousButton");

        verify(mediaManager, times(1)).setSeekPositionPercent(0d);
        verify(playlistManager, never()).playPreviousTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButton() {
        Playlist playlist = mock(Playlist.class);
        when(playlist.isEmpty()).thenReturn(true);
        when(playlistManager.getPlaylist(anyInt())).thenReturn(of(playlist));

        clickOnNode("#playPauseButton");

        verify(playlistManager, never()).pauseCurrentTrack();
        verify(playlistManager, never()).resumeCurrentTrack();
        verify(playlistManager, never()).playPlaylist(anyInt());
        verify(playlistManager, times(1)).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaying() {
        when(mediaManager.isPlaying()).thenReturn(true);

        clickOnNode("#playPauseButton");

        verify(playlistManager, times(1)).pauseCurrentTrack();
        verify(playlistManager, never()).resumeCurrentTrack();
        verify(playlistManager, never()).playPlaylist(anyInt());
        verify(playlistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPaused() {
        when(mediaManager.isPaused()).thenReturn(true);

        clickOnNode("#playPauseButton");

        verify(playlistManager, never()).pauseCurrentTrack();
        verify(playlistManager, times(1)).resumeCurrentTrack();
        verify(playlistManager, never()).playPlaylist(anyInt());
        verify(playlistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaylistSelected() {
        Playlist playlist = mock(Playlist.class);
        when(playlist.isEmpty()).thenReturn(false);
        when(playlistManager.getPlaylist(anyInt())).thenReturn(of(playlist));
        when(playlistManager.getSelectedTrack()).thenReturn(null);

        clickOnNode("#playPauseButton");

        verify(playlistManager, never()).pauseCurrentTrack();
        verify(playlistManager, never()).resumeCurrentTrack();
        verify(playlistManager, times(1)).playPlaylist(anyInt());
        verify(playlistManager, never()).playCurrentTrack(true);
    }

    @Test
    public void shouldClickPlayPauseButtonWhenPlaylistAndTrackSelected() {
        Playlist playlist = mock(Playlist.class);
        when(playlist.isEmpty()).thenReturn(false);
        when(playlistManager.getPlaylist(anyInt())).thenReturn(of(playlist));
        when(playlistManager.getSelectedTrack()).thenReturn(mock(Track.class));

        clickOnNode("#playPauseButton");

        verify(playlistManager, never()).pauseCurrentTrack();
        verify(playlistManager, never()).resumeCurrentTrack();
        verify(playlistManager, never()).playPlaylist(anyInt());
        verify(playlistManager, times(1)).playCurrentTrack(true);
    }

    @Test
    public void shouldClickNextButton() {
        clickOnNode("#nextButton");

        verify(playlistManager, times(1)).playNextTrack(true);
    }

    @Test
    public void shouldClickVolumeButtonWhenMuted() {
        when(mediaManager.isMuted()).thenReturn(true);

        clickOnNode("#volumeButton");

        verify(mediaManager, times(1)).setMuted();

        Button volumeButton = find("#volumeButton");
        assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_VOLUME_OFF + "')");
    }

    @Test
    public void shouldClickVolumeButtonWhenNotMuted() {
        when(mediaManager.isMuted()).thenReturn(false);

        clickOnNode("#volumeButton");

        verify(mediaManager, times(1)).setMuted();

        Button volumeButton = find("#volumeButton");
        assertThat(volumeButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_VOLUME_ON + "')");
    }

    @Test
    public void shouldClickShuffleButtonWhenShuffled() {
        when(playlistManager.isShuffle()).thenReturn(true);

        clickOnNode("#shuffleButton");

        verify(playlistManager, times(1)).setShuffle(false, false);

        Button shuffleButton = find("#shuffleButton");
        assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
    }

    @Test
    public void shouldClickShuffleButtonWhenNotShuffled() {
        when(playlistManager.isShuffle()).thenReturn(false);

        clickOnNode("#shuffleButton");

        verify(playlistManager, times(1)).setShuffle(true, false);

        Button shuffleButton = find("#shuffleButton");
        assertThat(shuffleButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatOff() {
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);

        clickOnNode("#repeatButton");

        verify(playlistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatOne() {
        when(playlistManager.getRepeat()).thenReturn(Repeat.ONE);

        clickOnNode("#repeatButton");

        verify(playlistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_ONE + "')");
    }

    @Test
    public void shouldClickRepeatButtonWhenRepeatAll() {
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        clickOnNode("#repeatButton");

        verify(playlistManager, times(1)).updateRepeat();

        Button repeatButton = find("#repeatButton");
        assertThat(repeatButton.getStyle()).isEqualTo("-fx-background-image: url('" + IMAGE_REPEAT_ALL + "')");
    }

    @Test
    public void shouldClickEqButton() {
        clickOnNode("#eqButton");

        verify(equalizerController, times(1)).updateSliderValues();
        verify(equalizerView, times(1)).show(true);
    }

    @Test
    @SneakyThrows
    public void shouldClickRandomButtonWithYearFilter() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(underTest, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().select(0));

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        List<Track> tracks = (List<Track>) mock(List.class);

        when(searchManager.getShuffledPlaylist(anyInt(), anyString())).thenReturn(tracks);

        clickOnNode("#randomButton");

        verify(searchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), "2000");
        verify(playlistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
        verify(playlistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Test
    @SneakyThrows
    public void shouldClickRandomButtonWithNoYearFilter() {
        @SuppressWarnings("unchecked")
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>) getNonNullField(underTest, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));

        threadRunner.runOnGui(() -> yearFilterComboBox.getSelectionModel().clearSelection());

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        List<Track> tracks = (List<Track>) mock(List.class);

        when(searchManager.getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null)).thenReturn(tracks);

        clickOnNode("#randomButton");

        verify(searchManager, times(1)).getShuffledPlaylist(appProperties.getShuffledPlaylistSize(), null);
        verify(playlistManager, times(1)).setPlaylistTracks(PLAYLIST_ID_SEARCH, tracks);
        verify(playlistManager, times(1)).playPlaylist(PLAYLIST_ID_SEARCH);
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
        when(playlistManager.getPlaylists()).thenReturn(playlists);
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(playlistManager.isShuffle()).thenReturn(false);
        when(mediaManager.isMuted()).thenReturn(false);
        when(searchManager.getYearList()).thenReturn(null);

        threadRunner.runOnGui(() -> underTest.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(1);
        assertThat(yearFilter.getYear()).isNull();

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(underTest, "observablePlaylists");
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
        when(playlistManager.getPlaylists()).thenReturn(playlists);
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(playlistManager.isShuffle()).thenReturn(false);
        when(mediaManager.isMuted()).thenReturn(false);
        when(searchManager.getYearList()).thenReturn(Collections.emptyList());

        threadRunner.runOnGui(() -> underTest.eventReceived(Event.APPLICATION_INITIALISED));

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
        when(playlistManager.getPlaylists()).thenReturn(playlists);
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(playlistManager.isShuffle()).thenReturn(false);
        when(mediaManager.isMuted()).thenReturn(false);
        when(searchManager.getYearList()).thenReturn(Arrays.asList("2000", "2001"));

        threadRunner.runOnGui(() -> underTest.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        ComboBox<YearFilter> yearFilterComboBox = find("#yearFilterComboBox");
        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();
        assertThat(yearFilterComboBox.getItems()).hasSize(3);
        assertThat(yearFilter.getYear()).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveApplicationInitialisedWithNoPlaylists() {
        when(playlistManager.getPlaylists()).thenReturn(Collections.emptyList());
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);
        when(playlistManager.isShuffle()).thenReturn(false);
        when(mediaManager.isMuted()).thenReturn(false);
        when(searchManager.getYearList()).thenReturn(null);

        threadRunner.runOnGui(() -> underTest.eventReceived(Event.APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getField(underTest, "observablePlaylists");
        assertThat(observablePlaylists).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveDataIndexed() {
        threadRunner.runOnGui(() -> underTest.eventReceived(Event.DATA_INDEXED));

        WaitForAsyncUtils.waitForFxEvents();

        verify(underTest, times(1)).updateYearFilter();
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

            underTest.eventReceived(Event.NEW_VERSION_AVAILABLE, version);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(newVersionButton.getText()).isEqualTo(messageManager.getMessage(MESSAGE_NEW_VERSION_AVAILABLE, version));
        assertThat(newVersionButton.isDisabled()).isFalse();
        assertThat(newVersionButton.isVisible()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMuteUpdated() {
        threadRunner.runOnGui(() -> underTest.eventReceived(Event.MUTE_UPDATED));

        WaitForAsyncUtils.waitForFxEvents();

        verify(underTest, times(1)).setVolumeButtonImage();
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

            underTest.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
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

            underTest.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
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

            underTest.eventReceived(Event.TIME_UPDATED, mediaDuration, currentTime);
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

            underTest.eventReceived(Event.BUFFER_UPDATED, mediaDuration, bufferProgressTime);
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

            underTest.eventReceived(Event.BUFFER_UPDATED, null, bufferProgressTime);
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

            underTest.eventReceived(Event.BUFFER_UPDATED, mediaDuration, null);
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

            underTest.eventReceived(Event.MEDIA_PLAYING);
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

            underTest.eventReceived(Event.MEDIA_PAUSED);
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

            underTest.eventReceived(Event.MEDIA_STOPPED);
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
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);

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

            underTest.eventReceived(Event.END_OF_MEDIA);
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
        when(playlistManager.getRepeat()).thenReturn(Repeat.ONE);

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

            underTest.eventReceived(Event.END_OF_MEDIA);
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
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, never()).updateObservablePlaylists();
        verify(playlistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedWithNullPayload() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_SELECTED, (Object[]) null);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, never()).updateObservablePlaylists();
        verify(playlistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedWithEmptyPayload() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_SELECTED);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, never()).updateObservablePlaylists();
        verify(playlistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isNull();
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isNull();
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedExistingPlaylist() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, never()).updateObservablePlaylists();
        verify(playlistManager, never()).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(search);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_SEARCH);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelectedPlaylistIsNotEmpty() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        favourites.addTrack(mock(Track.class));
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(true);

            underTest.eventReceived(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, never()).updateObservablePlaylists();
        verify(playlistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistDeleted() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_DELETED, PLAYLIST_ID_FAVOURITES);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, times(1)).updateObservablePlaylists();
        verify(playlistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(-1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWithEdit() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, true);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, times(1)).updateObservablePlaylists();
        verify(playlistManager, times(1)).clearSelectedTrack();

        assertThat(playlistPanelListView.getSelectionModel().getSelectedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getFocusModel().getFocusedItem()).isEqualTo(favourites);
        assertThat(playlistPanelListView.getEditingIndex()).isEqualTo(1);
        assertThat(currentSelectedPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(playPauseButton.isDisabled()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWithoutEdit() {
        setField(underTest, "currentSelectedPlaylistId", PLAYLIST_ID_SEARCH);

        Playlist search = new Playlist(PLAYLIST_ID_SEARCH, "Search", 10);
        Playlist favourites = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        when(playlistManager.getPlaylists()).thenReturn(Arrays.asList(search, favourites));
        when(playlistManager.getPlaylist(PLAYLIST_ID_SEARCH)).thenReturn(of(search));
        when(playlistManager.getPlaylist(PLAYLIST_ID_FAVOURITES)).thenReturn(of(favourites));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);

        @SuppressWarnings("unchecked")
        ObservableList<Playlist> observablePlaylists = (ObservableList<Playlist>) getNonNullField(underTest, "observablePlaylists");

        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");
        Button playPauseButton = find("#playPauseButton");

        threadRunner.runOnGui(() -> {
            observablePlaylists.add(search);
            observablePlaylists.add(favourites);
            playlistPanelListView.getSelectionModel().clearSelection();
            playlistPanelListView.getFocusModel().focus(-1);
            playPauseButton.setDisable(false);

            underTest.eventReceived(Event.PLAYLIST_CREATED, PLAYLIST_ID_FAVOURITES, false);
        });

        WaitForAsyncUtils.waitForFxEvents();

        int currentSelectedPlaylistId = (Integer) getNonNullField(underTest, "currentSelectedPlaylistId");

        verify(underTest, times(1)).updateObservablePlaylists();
        verify(playlistManager, times(1)).clearSelectedTrack();

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

            underTest.eventReceived(Event.TRACK_SELECTED);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playPauseButton.isDisabled()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlaying() {
        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        String albumImageUrl = "http://www.example.com/image.png";
        Image albumImage = new Image(albumImageUrl);

        when(cacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn(albumImageUrl);
        doAnswer(invocation -> {
            ImageView imageView = invocation.getArgument(0);
            imageView.setImage(albumImage);

            return null;
        }).when(imageFactory).loadImage(playingImageView, albumImageUrl);

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isEqualTo(track.getTrackName());
        assertThat(playingAlbumLabel.getText()).isEqualTo(track.getAlbumName());
        assertThat(playingArtistLabel.getText()).isEqualTo(track.getArtistName());
        assertThat(playingImageView.getImage()).isNotNull();
        assertThat(playPauseButton.isDisabled()).isTrue();

        verify(nativeManager, times(1)).displayNotification(track);
        verify(imageFactory, times(1)).loadImage(playingImageView, albumImageUrl);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlayingNullPayload() {
        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        when(cacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn("http://www.example.com/image.png");

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, (Object[]) null);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(nativeManager, never()).displayNotification(track);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlayingEmptyPayload() {
        Button playPauseButton = find("#playPauseButton");
        ImageView playingImageView = find("#playingImageView");
        Label playingTrackLabel = find("#playingTrackLabel");
        Label playingAlbumLabel = find("#playingAlbumLabel");
        Label playingArtistLabel = find("#playingArtistLabel");
        Track track = generateTrack(1);

        when(cacheManager.constructInternalUrl(any(), anyString(), anyString()))
                .thenReturn("http://www.example.com/image.png");

        threadRunner.runOnGui(() -> {
            playPauseButton.setDisable(false);
            playingImageView.setImage(null);
            playingTrackLabel.setText(null);
            playingAlbumLabel.setText(null);
            playingArtistLabel.setText(null);

            underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(playingTrackLabel.getText()).isNull();
        assertThat(playingAlbumLabel.getText()).isNull();
        assertThat(playingArtistLabel.getText()).isNull();
        assertThat(playingImageView.getImage()).isNull();
        assertThat(playPauseButton.isDisabled()).isFalse();

        verify(nativeManager, never()).displayNotification(track);
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

        verify(confirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(confirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(confirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(playlistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
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

        verify(confirmView, times(1))
                .setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(confirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(confirmView, times(1)).show(anyBoolean());

        okRunnable.getValue().run();

        verify(playlistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
    }

    @Test
    @SneakyThrows
    public void shouldTriggerOnKeyPressedOnPlaylistPanelListViewWithUnknownKey() {
        ListView<Playlist> playlistPanelListView = find("#playlistPanelListView");

        playlistPanelListView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.A));

        // Wait for the UI thread
        Thread.sleep(250);

        verify(confirmView, never()).show(anyBoolean());
    }

    @Test
    public void shouldReceiveMenuFileImportPlaylist() {
        doNothing().when(underTest).handleImportPlaylistButtonAction();

        underTest.eventReceived(MENU_FILE_IMPORT_PLAYLIST);

        verify(underTest, times(1)).handleImportPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuFileExportPlaylist() {
        doNothing().when(underTest).handleExportPlaylistButtonAction();

        underTest.eventReceived(MENU_FILE_EXPORT_PLAYLIST);

        verify(underTest, times(1)).handleExportPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuFileSettings() {
        doNothing().when(underTest).handleSettingsButtonAction();

        underTest.eventReceived(MENU_FILE_SETTINGS);

        verify(underTest, times(1)).handleSettingsButtonAction();
    }

    @Test
    public void shouldReceiveMenuEditAddPlaylist() {
        doNothing().when(underTest).handleAddPlaylistButtonAction();

        underTest.eventReceived(MENU_EDIT_ADD_PLAYLIST);

        verify(underTest, times(1)).handleAddPlaylistButtonAction();
    }

    @Test
    public void shouldReceiveMenuEditDeletePlaylist() {
        doNothing().when(underTest).handleDeletePlaylistButtonAction();

        underTest.eventReceived(MENU_EDIT_DELETE_PLAYLIST);

        verify(underTest, times(1)).handleDeletePlaylistButtonAction();
    }

    @Test
    public void shouldReceiveEditCreatePlaylistFromAlbumWithSelectedTrack() {
        Track mockTrack = mock(Track.class);

        when(trackTableController.getSelectedTrack()).thenReturn(mockTrack);

        underTest.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

        verify(playlistManager, times(1)).createPlaylistFromAlbum(mockTrack);
    }

    @Test
    public void shouldReceiveEditCreatePlaylistFromAlbumWithoutSelectedTrack() {
        when(trackTableController.getSelectedTrack()).thenReturn(null);

        underTest.eventReceived(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);

        verify(playlistManager, never()).createPlaylistFromAlbum(any());
    }

    @Test
    public void shouldReceiveMenuEditRandomPlaylist() {
        doNothing().when(underTest).handleRandomButtonAction();

        underTest.eventReceived(MENU_EDIT_RANDOM_PLAYLIST);

        verify(underTest, times(1)).handleRandomButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsPlayPause() {
        doNothing().when(underTest).handlePlayPauseButtonAction();

        underTest.eventReceived(MENU_CONTROLS_PLAY_PAUSE);

        verify(underTest, times(1)).handlePlayPauseButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsPrevious() {
        doNothing().when(underTest).handlePreviousButtonAction();

        underTest.eventReceived(MENU_CONTROLS_PREVIOUS);

        verify(underTest, times(1)).handlePreviousButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsNext() {
        doNothing().when(underTest).handleNextButtonAction();

        underTest.eventReceived(MENU_CONTROLS_NEXT);

        verify(underTest, times(1)).handleNextButtonAction();
    }

    @Test
    public void shouldReceiveMenuControlsShuffle() {
        doNothing().when(underTest).setShuffleButtonImage();

        underTest.eventReceived(MENU_CONTROLS_SHUFFLE);

        verify(underTest, times(1)).setShuffleButtonImage();
    }

    @Test
    public void shouldReceiveMenuControlsRepeat() {
        doNothing().when(underTest).setRepeatButtonImage();

        underTest.eventReceived(MENU_CONTROLS_REPEAT);

        verify(underTest, times(1)).setRepeatButtonImage();
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(20d);
        verify(mediaManager, times(1)).setVolumePercent(20d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithPayloadOver100() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(95d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_UP, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(100d);
        verify(mediaManager, times(1)).setVolumePercent(100d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeUpWithoutPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_UP);

        assertThat(volumeSlider.getValue()).isEqualTo(10d);
        verify(mediaManager, never()).setVolumePercent(anyDouble());
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(90d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(80d);
        verify(mediaManager, times(1)).setVolumePercent(80d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithPayloadBelowZero() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(5d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN, 10d);

        assertThat(volumeSlider.getValue()).isEqualTo(0d);
        verify(mediaManager, times(1)).setVolumePercent(0d);
    }

    @Test
    public void shouldReceiveMenuControlsVolumeDownWithoutPayload() {
        Slider volumeSlider = find("#volumeSlider");
        volumeSlider.setValue(10d);

        underTest.eventReceived(MENU_CONTROLS_VOLUME_DOWN);

        assertThat(volumeSlider.getValue()).isEqualTo(10d);
        verify(mediaManager, never()).setVolumePercent(anyDouble());
    }

    @Test
    public void shouldReceiveMenuControlsVolumeMute() {
        doNothing().when(underTest).handleVolumeButtonAction();

        underTest.eventReceived(MENU_CONTROLS_VOLUME_MUTE);

        verify(underTest, times(1)).handleVolumeButtonAction();
    }

    @Test
    public void shouldReceiveMenuViewEqualizer() {
        doNothing().when(underTest).handleEqButtonAction();

        underTest.eventReceived(MENU_VIEW_EQUALIZER);

        verify(underTest, times(1)).handleEqButtonAction();
    }

    @After
    public void cleanup() {
        setField(GUIState.class, "stage", existingStage);
    }
}
