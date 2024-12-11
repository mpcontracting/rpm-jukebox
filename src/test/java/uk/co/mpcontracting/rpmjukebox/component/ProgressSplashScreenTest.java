package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class ProgressSplashScreenTest extends AbstractGuiTest {

  private ProgressSplashScreen underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new ProgressSplashScreen();
  }

  @Test
  void shouldGetParent() {
    Parent parent = underTest.getParent();

    assertThat(parent).isNotNull();
  }

  @Test
  void shouldUpdateProgress() {
    String labelText = getFaker().lorem().characters(10, 25);
    Label label = mock(Label.class);
    setField(underTest, "progressLabel", label);

    underTest.updateProgress(labelText);

    verify(label).setText(labelText);
  }
}