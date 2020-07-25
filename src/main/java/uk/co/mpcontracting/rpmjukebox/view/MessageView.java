package uk.co.mpcontracting.rpmjukebox.view;

import javafx.scene.control.Label;
import uk.co.mpcontracting.rpmjukebox.javafx.FXMLView;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

@FXMLView(value = "/fxml/message.fxml", bundle = Constants.I18N_MESSAGE_BUNDLE)
public class MessageView extends AbstractModalView {

    public void setMessage(String message) {
        ContextHelper.lookup(getView().getScene().getRoot(), "message", Label.class).setText(message);
    }
}
