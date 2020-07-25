package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ProgressSplashScreenTest extends AbstractGUITest {

    private ProgressSplashScreen underTest;

    @Before
    public void setup() {
        underTest = new ProgressSplashScreen();
    }

    @Test
    public void shouldGetParent() {
        Parent parent = underTest.getParent();

        assertThat(parent).isNotNull();
    }

    @Test
    public void shouldUpdateProgress() {
        Label label = mock(Label.class);
        setField(underTest, "progressLabel", label);

        underTest.updateProgress("Test message");

        verify(label, times(1)).setText("Test message");
    }
}
