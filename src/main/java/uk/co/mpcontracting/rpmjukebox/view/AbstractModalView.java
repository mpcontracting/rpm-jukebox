package uk.co.mpcontracting.rpmjukebox.view;

import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.javafx.AbstractFxmlView;

import static java.util.Optional.ofNullable;

@Slf4j
public abstract class AbstractModalView extends AbstractFxmlView {

    private Stage owner;
    private Stage stage;
    private boolean blurBackground;

    public void initialise() {
        log.info("Initialising modal view - {}", getClass().getName());

        owner = RpmJukebox.getStage();

        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setScene(ofNullable(getView().getScene()).orElse(new Scene(getView(), Color.TRANSPARENT)));
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, windowEvent -> {
            stage.setX((owner.getX() + owner.getWidth() / 2) - stage.getWidth() / 2);
            stage.setY((owner.getY() + owner.getHeight() / 2) - stage.getHeight() / 2);
        });
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    public void show(boolean blurBackground) {
        this.blurBackground = blurBackground;

        if (blurBackground) {
            owner.getScene().getRoot().setEffect(new BoxBlur());
        }

        stage.show();
    }

    public void close() {
        if (blurBackground) {
            owner.getScene().getRoot().setEffect(null);
        }

        stage.close();
    }
}
