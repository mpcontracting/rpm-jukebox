package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ImageFactoryTest extends AbstractGUITest {

    @Mock
    private ImageView imageView;

    private ImageFactory underTest;

    @Before
    public void setup() {
        underTest = new ImageFactory();

        reset(imageView);
    }

    @Test
    @SneakyThrows
    public void shouldLoadImage() {
        underTest.loadImage(imageView, "http://www.example.com/image.png");

        // Wait for the UI thread
        Thread.sleep(2500);

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);

        verify(imageView, times(2)).setImage(imageCaptor.capture());

        assertThat(imageCaptor.getAllValues()).first().matches(image -> !isNull(image.getException()));
        assertThat(imageCaptor.getAllValues()).last().matches(image -> isNull(image.getException()));

        // 2nd time through should use the static default image - needed for coverage
        reset(imageView);

        underTest.loadImage(imageView, "http://www.example.com/image.png");

        // Wait for the UI thread
        Thread.sleep(250);

        verify(imageView, times(2)).setImage(imageCaptor.capture());

        assertThat(imageCaptor.getAllValues()).first().matches(image -> !isNull(image.getException()));
        assertThat(imageCaptor.getAllValues()).last().matches(image -> isNull(image.getException()));
    }
}