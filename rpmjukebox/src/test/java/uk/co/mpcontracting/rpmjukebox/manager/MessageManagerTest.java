package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;

public class MessageManagerTest extends AbstractTest {

	@Autowired
	private MessageManager messageManager;
	
	@Test
	public void shouldReturnCorrectMessageWithoutArguments() {
		String message = messageManager.getMessage("settings.copyright.2");
		
		assertThat("Message should be 'Version {0}'", message, equalTo("Version {0}"));
	}
	
	@Test
	public void shouldReturnCorrectMessageWithArguments() {
		String message = messageManager.getMessage("settings.copyright.2", "XXX");
		
		assertThat("Message should be 'Version XXX'", message, equalTo("Version XXX"));
	}
}
