package uk.co.mpcontracting.rpmjukebox.view;

import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.controller.ConfirmController;
import uk.co.mpcontracting.rpmjukebox.javafx.FXMLView;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

@FXMLView(value = "/fxml/confirm.fxml", bundle = Constants.I18N_MESSAGE_BUNDLE)
public class ConfirmView extends AbstractModalView {

    private ConfirmController confirmController;

    @Autowired
    private void wireConfirmController(ConfirmController confirmController) {
        this.confirmController = confirmController;
    }

    public void setMessage(String message) {
        ContextHelper.lookup(getView().getScene().getRoot(), "message", Label.class).setText(message);
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
