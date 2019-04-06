package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;

@Slf4j
@FXMLController
public class ConfirmController {

    @FXML
    private Button okButton;

    private ConfirmView confirmView;

    private Runnable okRunnable;
    private Runnable cancelRunnable;

    @Autowired
    private void wireConfirmView(ConfirmView confirmView) {
        this.confirmView = confirmView;
    }

    public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
        this.okRunnable = okRunnable;
        this.cancelRunnable = cancelRunnable;
    }

    public void setOkFocused() {
        okButton.requestFocus();
    }

    @FXML
    protected void handleOkButtonAction() {
        okButtonPressed();
    }

    @FXML
    protected void handleOkButtonKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            okButtonPressed();
        }
    }

    private void okButtonPressed() {
        log.debug("OK button pressed");

        if (okRunnable != null) {
            okRunnable.run();
        }

        confirmView.close();
    }

    @FXML
    protected void handleCancelButtonAction() {
        cancelButtonPressed();
    }

    @FXML
    protected void handleCancelButtonKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            cancelButtonPressed();
        }
    }

    private void cancelButtonPressed() {
        log.debug("Cancel button pressed");

        if (cancelRunnable != null) {
            cancelRunnable.run();
        }

        confirmView.close();
    }
}
