package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.javafx.SplashScreen;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
public class ProgressSplashScreen extends SplashScreen implements Constants {

    private Label progressLabel;

    @Override
    public Parent getParent() {
        ImageView imageView = new ImageView(getClass().getResource(IMAGE_SPLASH_SCREEN).toExternalForm());
        progressLabel = new Label();
        progressLabel.setStyle(loadStyle());
        progressLabel.setPrefWidth(imageView.getImage().getWidth());
        progressLabel.setPrefHeight(22);
        progressLabel.setText(null);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(imageView, progressLabel);

        return vbox;
    }

    private String loadStyle() {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/css/splash-screen.style")))) {
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
