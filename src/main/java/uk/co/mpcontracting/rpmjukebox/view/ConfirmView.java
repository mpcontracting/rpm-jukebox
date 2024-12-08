package uk.co.mpcontracting.rpmjukebox.view;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.I18N_MESSAGE_BUNDLE;

import de.felixroske.jfxsupport.FXMLView;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;

@FXMLView(value = "/fxml/confirm.fxml", bundle = I18N_MESSAGE_BUNDLE)
public class ConfirmView extends AbstractModalView {

  @Lazy
  @Autowired
  private ConfirmController confirmController;

  public void setMessage(String message) {
    ((Label) getView().getScene().getRoot().lookup("#message")).setText(message);
  }

  public void setRunnables(Runnable okRunnable, Runnable cancelRunnable) {
    confirmController.setRunnables(okRunnable, cancelRunnable);
  }

  @Override
  public void show(boolean blurBackground) {
    confirmController.setOkFocused();

    super.show(blurBackground);
  }
}
