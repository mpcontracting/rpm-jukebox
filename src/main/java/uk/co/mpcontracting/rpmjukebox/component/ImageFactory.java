package uk.co.mpcontracting.rpmjukebox.component;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_NO_ARTWORK;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;

@Component
public class ImageFactory {

  private static Image defaultImage;

  public void loadImage(ImageView imageView, String imageUrl) {
    Image image = new Image(imageUrl, true);
    image.errorProperty().addListener((observable, wasError, isError) -> imageView.setImage(getDefaultImage()));

    imageView.setImage(image);
  }

  private Image getDefaultImage() {
    synchronized (this) {
      if (defaultImage == null) {
        defaultImage = new Image(IMAGE_NO_ARTWORK);
      }

      return defaultImage;
    }
  }
}
