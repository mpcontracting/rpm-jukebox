package uk.co.mpcontracting.rpmjukebox.manager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerTest {

    @Mock
    private ResourceBundle resourceBundle;

    private MessageManager underTest;

    @Before
    public void setup() {
        underTest = new MessageManager();

        setField(underTest, "messageBundle", resourceBundle);

        when(resourceBundle.getString("settings.copyright.2")).thenReturn("Version {0}");
    }

    @Test
    public void shouldReturnCorrectMessageWithoutArguments() {
        String message = underTest.getMessage("settings.copyright.2");

        assertThat(message).isEqualTo("Version {0}");
    }

    @Test
    public void shouldReturnCorrectMessageWithArguments() {
        String message = underTest.getMessage("settings.copyright.2", "XXX");

        assertThat(message).isEqualTo("Version XXX");
    }
}
