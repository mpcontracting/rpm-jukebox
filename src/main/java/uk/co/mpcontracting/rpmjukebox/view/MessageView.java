package uk.co.mpcontracting.rpmjukebox.view;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.I18N_MESSAGE_BUNDLE;

import de.felixroske.jfxsupport.FXMLView;
import javafx.scene.control.Label;

@FXMLView(value = "/fxml/message.fxml", bundle = I18N_MESSAGE_BUNDLE)
public class MessageView extends AbstractModalView {

  public void setMessage(String message) {
    ((Label) getView().getScene().getRoot().lookup("#message")).setText(message);
  }
}
