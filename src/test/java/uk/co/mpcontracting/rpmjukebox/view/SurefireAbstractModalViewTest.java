package uk.co.mpcontracting.rpmjukebox.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import de.felixroske.jfxsupport.GUIState;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

// Needs to not start with the word Abstract or Surefire will ignore it
// Doesn't really matter which view we choose to use as the basis of the test
class SurefireAbstractModalViewTest extends AbstractGuiTest {

  @SpyBean
  private MessageView underTest;

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
  public void shouldInitialiseViewWithNewScene() {
    setField(underTest, "owner", null);
    setField(underTest, "stage", null);

    Platform.runLater(() -> underTest.initialise());

    WaitForAsyncUtils.waitForFxEvents();

    Stage owner = getField(underTest, "owner", Stage.class);
    Stage stage = getField(underTest, "stage", Stage.class);

    assertThat(owner).isNotNull();
    assertThat(stage).isNotNull();

    Stage spyStage = spy(stage);
    setField(underTest, "stage", spyStage);

    Platform.runLater(stage::show);

    WaitForAsyncUtils.waitForFxEvents();

    verify(spyStage).setX(anyDouble());
    verify(spyStage).setY(anyDouble());
  }

  @Test
  public void shouldInitialiseViewWithExistingScene() {
    setField(underTest, "owner", null);
    setField(underTest, "stage", null);

    when(underTest.getView()).thenReturn(new Group());

    Platform.runLater(() -> underTest.initialise());

    WaitForAsyncUtils.waitForFxEvents();

    Stage owner = getField(underTest, "owner", Stage.class);
    Stage stage = getField(underTest, "stage", Stage.class);

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

    boolean blurBackground = getField(underTest, "blurBackground", Boolean.class);

    assertThat(blurBackground).isTrue();
    verify(parent).setEffect(any());
    verify(stage).show();
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

    boolean blurBackground = getField(underTest, "blurBackground", Boolean.class);

    assertThat(blurBackground).isFalse();
    verify(parent, never()).setEffect(any());
    verify(stage).show();
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

    verify(parent).setEffect(null);
    verify(stage).close();
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
    verify(stage).close();
  }
}