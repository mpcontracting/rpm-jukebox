package uk.co.mpcontracting.rpmjukebox.view;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.javafx.GuiState;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

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
    private MessageView underTest;

    @Before
    public void setup() {
        underTest = spy(messageView);
        originalStage = GuiState.getStage();
        setField(GuiState.class, "stage", mock(Stage.class));
    }

    // Testing the AbstractModelView here as SureFire doesn't pick
    // up tests starting with the word Abstract

    @Test
    public void shouldInitialiseViewWithNewScene() {
        setField(underTest, "owner", null);
        setField(underTest, "stage", null);

        threadRunner.runOnGui(() -> underTest.initialise());

        WaitForAsyncUtils.waitForFxEvents();

        Stage owner = (Stage) ReflectionTestUtils.getField(underTest, "owner");
        Stage stage = (Stage) ReflectionTestUtils.getField(underTest, "stage");

        assertThat(owner).isNotNull();
        assertThat(stage).isNotNull();

        Stage spyStage = spy(stage);
        setField(underTest, "stage", spyStage);

        threadRunner.runOnGui(stage::show);

        WaitForAsyncUtils.waitForFxEvents();

        verify(spyStage, times(1)).setX(anyDouble());
        verify(spyStage, times(1)).setY(anyDouble());
    }

    @Test
    public void shouldInitialiseViewWithExistingScene() {
        setField(underTest, "owner", null);
        setField(underTest, "stage", null);

        when(underTest.getView()).thenReturn(new Group());

        threadRunner.runOnGui(() -> underTest.initialise());

        WaitForAsyncUtils.waitForFxEvents();

        Stage owner = (Stage) ReflectionTestUtils.getField(underTest, "owner");
        Stage stage = (Stage) ReflectionTestUtils.getField(underTest, "stage");

        assertThat(owner).isNotNull();
        assertThat(stage).isNotNull();
    }

    @Test
    public void shouldGetIsShowing() {
        Stage stage = mock(Stage.class);
        when(stage.isShowing()).thenReturn(true);

        setField(underTest, "stage", stage);

        boolean result = underTest.isShowing();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldShowWithBlur() {
        Parent parent = mock(Parent.class);

        Scene scene = mock(Scene.class);
        when(scene.getRoot()).thenReturn(parent);

        Stage owner = mock(Stage.class);
        when(owner.getScene()).thenReturn(scene);

        Stage stage = mock(Stage.class);

        setField(underTest, "owner", owner);
        setField(underTest, "stage", stage);

        underTest.show(true);

        boolean blurBackground = (boolean) ReflectionTestUtils.getField(underTest, "blurBackground");

        assertThat(blurBackground).isTrue();
        verify(parent, times(1)).setEffect(any());
        verify(stage, times(1)).show();
    }

    @Test
    public void shouldShowWithoutBlur() {
        Parent parent = mock(Parent.class);

        Scene scene = mock(Scene.class);
        when(scene.getRoot()).thenReturn(parent);

        Stage owner = mock(Stage.class);
        when(owner.getScene()).thenReturn(scene);

        Stage stage = mock(Stage.class);

        setField(underTest, "owner", owner);
        setField(underTest, "stage", stage);

        underTest.show(false);

        boolean blurBackground = (boolean) ReflectionTestUtils.getField(underTest, "blurBackground");

        assertThat(blurBackground).isFalse();
        verify(parent, never()).setEffect(any());
        verify(stage, times(1)).show();
    }

    @Test
    public void shouldCloseWithBlur() {
        Parent parent = mock(Parent.class);

        Scene scene = mock(Scene.class);
        when(scene.getRoot()).thenReturn(parent);

        Stage owner = mock(Stage.class);
        when(owner.getScene()).thenReturn(scene);

        Stage stage = mock(Stage.class);

        setField(underTest, "owner", owner);
        setField(underTest, "stage", stage);
        setField(underTest, "blurBackground", true);

        underTest.close();

        verify(parent, times(1)).setEffect(null);
        verify(stage, times(1)).close();
    }

    @Test
    public void shouldCloseWithoutBlur() {
        Parent parent = mock(Parent.class);

        Scene scene = mock(Scene.class);
        when(scene.getRoot()).thenReturn(parent);

        Stage owner = mock(Stage.class);
        when(owner.getScene()).thenReturn(scene);

        Stage stage = mock(Stage.class);

        setField(underTest, "owner", owner);
        setField(underTest, "stage", stage);
        setField(underTest, "blurBackground", false);

        underTest.close();

        verify(parent, never()).setEffect(null);
        verify(stage, times(1)).close();
    }

    @Test
    public void shouldSetMessage() {
        Parent view = mock(Parent.class);
        when(underTest.getView()).thenReturn(view);

        Scene scene = mock(Scene.class);
        when(view.getScene()).thenReturn(scene);

        Parent root = mock(Parent.class);
        when(scene.getRoot()).thenReturn(root);

        Label label = mock(Label.class);
        when(root.lookup("#message")).thenReturn(label);

        underTest.setMessage("Test Message");

        verify(label, times(1)).setText("Test Message");
    }

    @After
    public void cleanup() {
        setField(GuiState.class, "stage", originalStage);
    }
}
