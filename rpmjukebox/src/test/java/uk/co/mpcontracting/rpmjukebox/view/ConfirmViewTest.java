package uk.co.mpcontracting.rpmjukebox.view;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import de.felixroske.jfxsupport.GUIState;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class ConfirmViewTest extends AbstractTest {

    @Autowired
    private ConfirmView confirmView;

    @Mock
    private ConfirmController mockConfirmController;

    private Stage originalStage;
    private ConfirmView spyConfirmView;

    @Before
    public void setup() {
        spyConfirmView = spy(confirmView);
        originalStage = GUIState.getStage();
        ReflectionTestUtils.setField(GUIState.class, "stage", mock(Stage.class));
        ReflectionTestUtils.setField(spyConfirmView, "confirmController", mockConfirmController);
    }

    @Test
    public void shouldSetMessage() {
        Parent mockView = mock(Parent.class);
        when(spyConfirmView.getView()).thenReturn(mockView);

        Scene mockScene = mock(Scene.class);
        when(mockView.getScene()).thenReturn(mockScene);

        Parent mockRoot = mock(Parent.class);
        when(mockScene.getRoot()).thenReturn(mockRoot);

        Label mockLabel = mock(Label.class);
        when(mockRoot.lookup("#message")).thenReturn(mockLabel);

        spyConfirmView.setMessage("Test Message");

        verify(mockLabel, times(1)).setText("Test Message");
    }

    @Test
    public void shouldSetRunnables() {
        spyConfirmView.setRunnables(null, null);

        verify(mockConfirmController, times(1)).setRunnables(any(), any());
    }

    @Test
    public void shouldShow() {
        ReflectionTestUtils.setField(spyConfirmView, "stage", mock(Stage.class));

        spyConfirmView.show(false);

        verify(mockConfirmController, times(1)).setOkFocused();
    }

    @After
    public void cleanup() {
        ReflectionTestUtils.setField(GUIState.class, "stage", originalStage);
    }
}
