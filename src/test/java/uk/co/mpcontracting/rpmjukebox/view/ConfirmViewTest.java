package uk.co.mpcontracting.rpmjukebox.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import de.felixroske.jfxsupport.GUIState;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper;

class ConfirmViewTest extends AbstractGuiTest {

  @MockBean
  private ConfirmController confirmController;

  @SpyBean
  private ConfirmView underTest;

  private Stage originalStage;

  @BeforeEach
  void beforeEach() {
    originalStage = GUIState.getStage();
    setField(GUIState.class, "stage", mock(Stage.class));
  }

  @AfterEach
  void afterEach() {
    setField(GUIState.class, "stage", originalStage);
  }

  @Test
  void shouldSetMessage() {
    Parent view = mock(Parent.class);
    when(underTest.getView()).thenReturn(view);

    Scene scene = mock(Scene.class);
    when(view.getScene()).thenReturn(scene);

    Parent root = mock(Parent.class);
    when(scene.getRoot()).thenReturn(root);

    Label label = mock(Label.class);
    when(root.lookup("#message")).thenReturn(label);

    String message = TestDataHelper.getFaker().lorem().characters(20, 50);

    underTest.setMessage(message);

    verify(label).setText(message);
  }

  @Test
  void shouldSetRunnables() {
    underTest.setRunnables(null, null);

    verify(confirmController).setRunnables(any(), any());
  }

  @Test
  void shouldShow() {
    setField(underTest, "stage", mock(Stage.class));

    underTest.show(false);

    verify(confirmController).setOkFocused();
  }
}