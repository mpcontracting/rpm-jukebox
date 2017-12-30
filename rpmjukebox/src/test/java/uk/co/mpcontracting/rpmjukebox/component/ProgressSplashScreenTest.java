package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class ProgressSplashScreenTest extends AbstractTest {

    @Test
    public void shouldGetParent() {
        ProgressSplashScreen splashScreen = new ProgressSplashScreen();
        Parent parent = splashScreen.getParent();

        assertThat("Parent should not be null", parent, notNullValue());
    }

    @Test
    public void shouldUpdateProgress() {
        ProgressSplashScreen splashScreen = new ProgressSplashScreen();
        Label mockProgressLabel = mock(Label.class);
        ReflectionTestUtils.setField(splashScreen, "progressLabel", mockProgressLabel);

        splashScreen.updateProgress("Test message");

        verify(mockProgressLabel, times(1)).setText("Test message");
    }
}
