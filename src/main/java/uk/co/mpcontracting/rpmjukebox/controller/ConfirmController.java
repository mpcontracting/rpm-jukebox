package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Optional.ofNullable;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class ConfirmController {

  @FXML
  private Button okButton;

  private final ConfirmView confirmView;

  private Runnable okRunnable;
  private Runnable cancelRunnable;

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
  protected void handleOkButtonKeyPressed(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.ENTER) {
      okButtonPressed();
    }
  }

  private void okButtonPressed() {
    log.debug("OK button pressed");

    ofNullable(okRunnable).ifPresent(Runnable::run);

    confirmView.close();
  }

  @FXML
  protected void handleCancelButtonAction() {
    cancelButtonPressed();
  }

  @FXML
  protected void handleCancelButtonKeyPressed(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.ENTER) {
      cancelButtonPressed();
    }
  }

  private void cancelButtonPressed() {
    log.debug("Cancel button pressed");

    ofNullable(cancelRunnable).ifPresent(Runnable::run);

    confirmView.close();
  }
}
