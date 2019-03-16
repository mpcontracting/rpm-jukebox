package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ProgressSplashScreenTest extends AbstractTest {

    @Test
    public void shouldGetParent() {
        ProgressSplashScreen splashScreen = new ProgressSplashScreen();
        Parent parent = splashScreen.getParent();

        assertThat(parent).isNotNull();
    }

    @Test
    public void shouldUpdateProgress() {
        ProgressSplashScreen splashScreen = new ProgressSplashScreen();
        Label mockProgressLabel = mock(Label.class);
        setField(splashScreen, "progressLabel", mockProgressLabel);

        splashScreen.updateProgress("Test message");

        verify(mockProgressLabel, times(1)).setText("Test message");
    }
}
