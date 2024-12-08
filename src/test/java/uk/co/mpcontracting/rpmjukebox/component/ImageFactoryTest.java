package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class ImageFactoryTest extends AbstractGuiTest {

  @Mock
  private ImageView imageView;

  private ImageFactory underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new ImageFactory();
  }

  @Test
  @SneakyThrows
  void shouldLoadImage() {
    String imageUrl = "http://www.example.com/image.png";

    underTest.loadImage(imageView, imageUrl);

    // Wait for the UI thread
    Thread.sleep(2500);

    ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);

    verify(imageView, times(2)).setImage(imageCaptor.capture());

    assertThat(imageCaptor.getAllValues()).first().matches(image -> !isNull(image.getException()));
    assertThat(imageCaptor.getAllValues()).last().matches(image -> isNull(image.getException()));

    // 2nd time through should use the static default image - needed for coverage
    reset(imageView);

    underTest.loadImage(imageView, imageUrl);

    // Wait for the UI thread
    Thread.sleep(250);

    verify(imageView, times(2)).setImage(imageCaptor.capture());

    assertThat(imageCaptor.getAllValues()).first().matches(image -> !isNull(image.getException()));
    assertThat(imageCaptor.getAllValues()).last().matches(image -> isNull(image.getException()));
  }
}