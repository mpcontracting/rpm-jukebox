package uk.co.mpcontracting.rpmjukebox.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.NativeManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.manager.UpdateManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;
import uk.co.mpcontracting.rpmjukebox.view.MessageView;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

public class MainPanelControllerTest extends AbstractTest implements Constants {

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
    
    @PostConstruct
    public void constructView() throws Exception {
        init(mainPanelView);
    }
    
    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        ReflectionTestUtils.setField(mainPanelController, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(mainPanelController, "equalizerView", mockEqualizerView);
        ReflectionTestUtils.setField(mainPanelController, "settingsView", mockSettingsView);
        ReflectionTestUtils.setField(mainPanelController, "exportView", mockExportView);
        ReflectionTestUtils.setField(mainPanelController, "messageView", mockMessageView);
        ReflectionTestUtils.setField(mainPanelController, "confirmView", mockConfirmView);
        ReflectionTestUtils.setField(mainPanelController, "trackTableView", mockTrackTableView);
        ReflectionTestUtils.setField(mainPanelController, "equalizerController", mockEqualizerController);
        ReflectionTestUtils.setField(mainPanelController, "settingsController", mockSettingsController);
        ReflectionTestUtils.setField(mainPanelController, "exportController", mockExportController);
        ReflectionTestUtils.setField(mainPanelController, "settingsManager", mockSettingsManager);
        ReflectionTestUtils.setField(mainPanelController, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(mainPanelController, "playlistManager", mockPlaylistManager);
        ReflectionTestUtils.setField(mainPanelController, "mediaManager", mockMediaManager);
        ReflectionTestUtils.setField(mainPanelController, "cacheManager", mockCacheManager);
        ReflectionTestUtils.setField(mainPanelController, "nativeManager", mockNativeManager);
        ReflectionTestUtils.setField(mainPanelController, "updateManager", mockUpdateManager);
        
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
            ((TextField)ReflectionTestUtils.getField(mainPanelController, "searchTextField")).setText(null);
            ((ComboBox<YearFilter>)ReflectionTestUtils.getField(mainPanelController, "yearFilterComboBox")).getItems().clear();
            ((ListView<Playlist>)ReflectionTestUtils.getField(mainPanelController, "playlistPanelListView")).getItems().clear();
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
        TextField searchTextField = (TextField)ReflectionTestUtils.getField(mainPanelController, "searchTextField");
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
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
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>)ReflectionTestUtils.getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));
        
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
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
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>)ReflectionTestUtils.getField(mainPanelController, "yearFilterComboBox");
        yearFilterComboBox.getItems().add(new YearFilter("2000", "2000"));
        yearFilterComboBox.getItems().add(new YearFilter("2001", "2001"));
        yearFilterComboBox.getItems().add(new YearFilter("2002", "2002"));
        
        TextField searchTextField = (TextField)ReflectionTestUtils.getField(mainPanelController, "searchTextField");
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
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
        ComboBox<YearFilter> yearFilterComboBox = (ComboBox<YearFilter>)ReflectionTestUtils.getField(mainPanelController, "yearFilterComboBox");
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
        
        ThreadRunner.runOnGui(() -> {
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
        Playlist playlist = new Playlist(1, "Playlist 1", 10);
        
        @SuppressWarnings("unchecked")
        ListView<Playlist> playlistPanelListView = (ListView<Playlist>)ReflectionTestUtils.getField(mainPanelController, "playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
            playlistPanelListView.getItems().add(playlist);
            playlistPanelListView.getSelectionModel().select(0);
            latch.countDown();
        });
        
        latch.await(2000, TimeUnit.MILLISECONDS);

        clickOnNode("#deletePlaylistButton");
        
        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mockConfirmView, times(1)).setMessage(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()));
        verify(mockConfirmView, times(1)).setRunnables(okRunnable.capture(), any());
        verify(mockConfirmView, times(1)).show(anyBoolean());
        
        okRunnable.getValue().run();
        
        verify(mockPlaylistManager, times(1)).deletePlaylist(playlist.getPlaylistId());
    }
    
    @Test
    public void shouldClickDeletePlaylistButtonWithReservedPlaylist() throws Exception {
        Playlist playlist = new Playlist(PLAYLIST_ID_SEARCH, "Playlist 1", 10);
        
        @SuppressWarnings("unchecked")
        ListView<Playlist> playlistPanelListView = (ListView<Playlist>)ReflectionTestUtils.getField(mainPanelController, "playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
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
        @SuppressWarnings("unchecked")
        ListView<Playlist> playlistPanelListView = (ListView<Playlist>)ReflectionTestUtils.getField(mainPanelController, "playlistPanelListView");

        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
            playlistPanelListView.getSelectionModel().clearSelection();
            latch.countDown();
        });
        
        latch.await(2000, TimeUnit.MILLISECONDS);
        
        clickOnNode("#deletePlaylistButton");
        
        verify(mockConfirmView, never()).show(anyBoolean());
    }
}
