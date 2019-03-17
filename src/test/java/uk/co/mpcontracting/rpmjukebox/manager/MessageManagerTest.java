package uk.co.mpcontracting.rpmjukebox.manager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ResourceBundle;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerTest {

    @Mock
    private ResourceBundle mockResourceBundle;

    private MessageManager messageManager;

    @Before
    public void setup() {
        messageManager = new MessageManager();

        setField(messageManager, "messageBundle", mockResourceBundle);

        when(mockResourceBundle.getString("settings.copyright.2")).thenReturn("Version {0}");
    }

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
