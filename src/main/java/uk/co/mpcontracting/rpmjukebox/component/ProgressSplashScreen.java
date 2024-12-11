package uk.co.mpcontracting.rpmjukebox.component;

import static java.util.Objects.requireNonNull;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_SPLASH_SCREEN;

import de.felixroske.jfxsupport.SplashScreen;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressSplashScreen extends SplashScreen {

  private Label progressLabel;

  @Override
  public Parent getParent() {
    ImageView imageView = new ImageView(getClass().getResource(IMAGE_SPLASH_SCREEN).toExternalForm());
    progressLabel = new Label();
    progressLabel.setStyle(loadStyle());
    progressLabel.setPrefWidth(imageView.getImage().getWidth());
    progressLabel.setPrefHeight(22);
    progressLabel.setText(null);

    StackPane stackPane = new StackPane();
    stackPane.getChildren().addAll(imageView, progressLabel);

    return stackPane;
  }

  private String loadStyle() {
    StringBuilder builder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader( requireNonNull(getClass().getResourceAsStream("/css/splash-screen.style")))
    )) {
      reader.lines().forEach(line -> builder.append(line).append(' '));
    } catch (Exception e) {
      log.error("Unable to load splash screen CSS", e);
    }

    return builder.toString();
  }

  public void updateProgress(String message) {
    log.debug("Updating progress - {}", message);

    progressLabel.setText(message);
  }
}
