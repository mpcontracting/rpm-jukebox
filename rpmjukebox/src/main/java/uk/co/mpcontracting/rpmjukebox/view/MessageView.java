package uk.co.mpcontracting.rpmjukebox.view;

import de.felixroske.jfxsupport.FXMLView;
import javafx.scene.control.Label;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@FXMLView(value = "/fxml/message.fxml", bundle = Constants.I18N_MESSAGE_BUNDLE)
public class MessageView extends AbstractModalView {

    public void setMessage(String message) {
        checkInitialised();
        
        FxmlContext.lookup(getView().getScene().getRoot(), "message", Label.class).setText(message);
    }
}
