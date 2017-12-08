package uk.co.mpcontracting.rpmjukebox.view;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import de.felixroske.jfxsupport.AbstractFxmlView;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

public abstract class AbstractModalView extends AbstractFxmlView implements InitializingBean {

    private Stage owner;
    private Stage stage;
    private boolean blurBackground;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
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
            
            latch.countDown();
        });
        
        latch.await(2000, TimeUnit.MILLISECONDS);
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
