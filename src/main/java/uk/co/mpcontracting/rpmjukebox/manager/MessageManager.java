package uk.co.mpcontracting.rpmjukebox.manager;

import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@Component
public class MessageManager implements Constants {

    private ResourceBundle messageBundle;

    public MessageManager() {
        // Load up the resource bundle
        messageBundle = ResourceBundle.getBundle(I18N_MESSAGE_BUNDLE);
    }

    public String getMessage(String key, Object... arguments) {
        return MessageFormat.format(messageBundle.getString(key), arguments);
    }
}
