package uk.co.mpcontracting.rpmjukebox.manager;

import java.util.ResourceBundle;

import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Component
public class MessageManager implements Constants {

	private ResourceBundle messageBundle;
	
	public MessageManager() {
		// Load up the resource bundle
		messageBundle = ResourceBundle.getBundle(I18N_MESSAGE_BUNDLE);
	}
	
	public String getMessage(String key, Object... arguments) {
		return messageBundle.getString(key);
	}
}
