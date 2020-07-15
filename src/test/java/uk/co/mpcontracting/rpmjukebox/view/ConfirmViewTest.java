package uk.co.mpcontracting.rpmjukebox.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;
import uk.co.mpcontracting.rpmjukebox.javafx.GUIState;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ConfirmViewTest extends AbstractGUITest {

    @Autowired
    private ConfirmView confirmView;

    @Mock
    private ConfirmController confirmController;

    private Stage originalStage;
    private ConfirmView underTest;

    @Before
    public void setup() {
        underTest = spy(confirmView);
        originalStage = GUIState.getStage();
        setField(GUIState.class, "stage", mock(Stage.class));
        setField(underTest, "confirmController", confirmController);
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

    @Test
    public void shouldSetRunnables() {
        underTest.setRunnables(null, null);

        verify(confirmController, times(1)).setRunnables(any(), any());
    }

    @Test
    public void shouldShow() {
        setField(underTest, "stage", mock(Stage.class));

        underTest.show(false);

        verify(confirmController, times(1)).setOkFocused();
    }

    @After
    public void cleanup() {
        setField(GUIState.class, "stage", originalStage);
    }
}
