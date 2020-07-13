package uk.co.mpcontracting.rpmjukebox.view;

import de.felixroske.jfxsupport.GUIState;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class MessageViewTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private MessageView messageView;

    private Stage originalStage;
    private MessageView spyMessageView;

    @Before
    public void setup() {
        spyMessageView = spy(messageView);
        originalStage = GUIState.getStage();
        setField(GUIState.class, "stage", mock(Stage.class));
    }

    // Testing the AbstractModelView here as SureFire doesn't pick
    // up tests starting with the word Abstract

    @Test
    public void shouldInitialiseViewWithNewScene() throws Exception {
        setField(spyMessageView, "owner", null);
        setField(spyMessageView, "stage", null);
        CountDownLatch latch1 = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            spyMessageView.initialise();
            latch1.countDown();
        });

        latch1.await(2000, TimeUnit.MILLISECONDS);

        Stage owner = (Stage) ReflectionTestUtils.getField(spyMessageView, "owner");
        Stage stage = (Stage) ReflectionTestUtils.getField(spyMessageView, "stage");

        assertThat(owner).isNotNull();
        assertThat(stage).isNotNull();

        Stage spyStage = spy(stage);
        setField(spyMessageView, "stage", spyStage);

        CountDownLatch latch2 = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            stage.show();
            latch2.countDown();
        });

        latch2.await(2000, TimeUnit.MILLISECONDS);

        verify(spyStage, times(1)).setX(anyDouble());
        verify(spyStage, times(1)).setY(anyDouble());
    }

    @Test
    public void shouldInitialiseViewWithExistingScene() throws Exception {
        setField(spyMessageView, "owner", null);
        setField(spyMessageView, "stage", null);

        Parent mockParent = mock(Parent.class);
        Scene mockScene = mock(Scene.class);
        when(mockParent.getScene()).thenReturn(mockScene);
        when(mockParent.getStyleClass()).thenReturn(FXCollections.observableArrayList());
        when(spyMessageView.getView()).thenReturn(mockParent);

        CountDownLatch latch = new CountDownLatch(1);

        threadRunner.runOnGui(() -> {
            spyMessageView.initialise();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        Stage owner = (Stage) ReflectionTestUtils.getField(spyMessageView, "owner");
        Stage stage = (Stage) ReflectionTestUtils.getField(spyMessageView, "stage");

        assertThat(owner).isNotNull();
        assertThat(stage).isNotNull();
    }

    @Test
    public void shouldGetIsShowing() {
        Stage mockStage = mock(Stage.class);
        when(mockStage.isShowing()).thenReturn(true);

        setField(spyMessageView, "stage", mockStage);

        boolean result = spyMessageView.isShowing();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldShowWithBlur() {
        Parent mockParent = mock(Parent.class);

        Scene mockScene = mock(Scene.class);
        when(mockScene.getRoot()).thenReturn(mockParent);

        Stage mockOwner = mock(Stage.class);
        when(mockOwner.getScene()).thenReturn(mockScene);

        Stage mockStage = mock(Stage.class);

        setField(spyMessageView, "owner", mockOwner);
        setField(spyMessageView, "stage", mockStage);

        spyMessageView.show(true);

        boolean blurBackground = (boolean) ReflectionTestUtils.getField(spyMessageView, "blurBackground");

        assertThat(blurBackground).isTrue();
        verify(mockParent, times(1)).setEffect(any());
        verify(mockStage, times(1)).show();
    }

    @Test
    public void shouldShowWithoutBlur() {
        Parent mockParent = mock(Parent.class);

        Scene mockScene = mock(Scene.class);
        when(mockScene.getRoot()).thenReturn(mockParent);

        Stage mockOwner = mock(Stage.class);
        when(mockOwner.getScene()).thenReturn(mockScene);

        Stage mockStage = mock(Stage.class);

        setField(spyMessageView, "owner", mockOwner);
        setField(spyMessageView, "stage", mockStage);

        spyMessageView.show(false);

        boolean blurBackground = (boolean) ReflectionTestUtils.getField(spyMessageView, "blurBackground");

        assertThat(blurBackground).isFalse();
        verify(mockParent, never()).setEffect(any());
        verify(mockStage, times(1)).show();
    }

    @Test
    public void shouldCloseWithBlur() {
        Parent mockParent = mock(Parent.class);

        Scene mockScene = mock(Scene.class);
        when(mockScene.getRoot()).thenReturn(mockParent);

        Stage mockOwner = mock(Stage.class);
        when(mockOwner.getScene()).thenReturn(mockScene);

        Stage mockStage = mock(Stage.class);

        setField(spyMessageView, "owner", mockOwner);
        setField(spyMessageView, "stage", mockStage);
        setField(spyMessageView, "blurBackground", true);

        spyMessageView.close();

        verify(mockParent, times(1)).setEffect(null);
        verify(mockStage, times(1)).close();
    }

    @Test
    public void shouldCloseWithoutBlur() {
        Parent mockParent = mock(Parent.class);

        Scene mockScene = mock(Scene.class);
        when(mockScene.getRoot()).thenReturn(mockParent);

        Stage mockOwner = mock(Stage.class);
        when(mockOwner.getScene()).thenReturn(mockScene);

        Stage mockStage = mock(Stage.class);

        setField(spyMessageView, "owner", mockOwner);
        setField(spyMessageView, "stage", mockStage);
        setField(spyMessageView, "blurBackground", false);

        spyMessageView.close();

        verify(mockParent, never()).setEffect(null);
        verify(mockStage, times(1)).close();
    }

    @Test
    public void shouldSetMessage() {
        Parent mockView = mock(Parent.class);
        when(spyMessageView.getView()).thenReturn(mockView);

        Scene mockScene = mock(Scene.class);
        when(mockView.getScene()).thenReturn(mockScene);

        Parent mockRoot = mock(Parent.class);
        when(mockScene.getRoot()).thenReturn(mockRoot);

        Label mockLabel = mock(Label.class);
        when(mockRoot.lookup("#message")).thenReturn(mockLabel);

        spyMessageView.setMessage("Test Message");

        verify(mockLabel, times(1)).setText("Test Message");
    }

    @After
    public void cleanup() {
        setField(GUIState.class, "stage", originalStage);
    }
}
