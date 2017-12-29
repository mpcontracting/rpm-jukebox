package uk.co.mpcontracting.rpmjukebox.component;

import de.felixroske.jfxsupport.SplashScreen;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressSplashScreen extends SplashScreen {

    @Override
    public Parent getParent() {
        final ImageView imageView = new ImageView(getClass().getResource(getImagePath()).toExternalForm());
        final ProgressBar splashProgressBar = new ProgressBar();
        splashProgressBar.setPrefWidth(imageView.getImage().getWidth());

        final VBox vbox = new VBox();
        vbox.getChildren().addAll(imageView, splashProgressBar);

        return vbox;
    }

    public void updateProgress(String message) {
        log.debug("Updating progress - {}", message);
    }
}
