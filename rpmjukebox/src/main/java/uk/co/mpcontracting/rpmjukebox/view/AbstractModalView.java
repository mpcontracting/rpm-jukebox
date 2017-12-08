package uk.co.mpcontracting.rpmjukebox.view;

import de.felixroske.jfxsupport.AbstractFxmlView;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.Synchronized;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;

public abstract class AbstractModalView extends AbstractFxmlView {

    private Stage owner;
    private Stage stage;
    private boolean blurBackground;

    @Synchronized
    protected void checkInitialised() {
        if (owner == null) {
            owner = RpmJukebox.getStage();
            
            stage = new Stage(StageStyle.TRANSPARENT);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
    
            Scene scene;
            
            if (getView().getScene() != null) {
                scene = getView().getScene();
            } else {
                scene = new Scene(getView(), Color.TRANSPARENT);
            }
            
            stage.setScene(scene);
            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
                stage.setX((owner.getX() + owner.getWidth() / 2) - stage.getWidth() / 2);
                stage.setY((owner.getY() + owner.getHeight() / 2) - stage.getHeight() / 2);
            });
        }
    }
    
    public boolean isShowing() {
        checkInitialised();
        
        return stage.isShowing();
    }
    
    public void show(boolean blurBackground) {
        checkInitialised();
        
        this.blurBackground = blurBackground;
        
        if (blurBackground) {
            owner.getScene().getRoot().setEffect(new BoxBlur());
        }
        
        stage.show();
    }

    public void close() {
        checkInitialised();
        
        if (blurBackground) {
            owner.getScene().getRoot().setEffect(null);
        }
        
        stage.close();
    }
}
