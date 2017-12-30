package uk.co.mpcontracting.rpmjukebox.component;

import de.felixroske.jfxsupport.SplashScreen;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
public class ProgressSplashScreen extends SplashScreen implements Constants {

    private Label progressLabel;

    @Override
    public Parent getParent() {
        ImageView imageView = new ImageView(getClass().getResource(IMAGE_SPLASH_SCREEN).toExternalForm());
        progressLabel = new Label();
        progressLabel.setStyle(
            "-fx-background-color: white; -fx-text-fill: black; -fx-padding: 0 0 0 3; -fx-border-color: -black; -fx-border-style: solid; -fx-border-width: 0 1 1 1;");
        progressLabel.setPrefWidth(imageView.getImage().getWidth());
        progressLabel.setText(null);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(imageView, progressLabel);

        return vbox;
    }

    public void updateProgress(String message) {
        log.debug("Updating progress - {}", message);

        progressLabel.setText(message);
    }
}
