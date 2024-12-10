package uk.co.mpcontracting.rpmjukebox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringResourceServiceTest {

  @Mock
  private ResourceBundle resourceBundle;

  private StringResourceService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new StringResourceService();

    setField(underTest, "resourceBundle", resourceBundle);

    when(resourceBundle.getString("settings.copyright.2")).thenReturn("Version {0}");
  }

  @Test
  void shouldReturnCorrectMessageWithoutArguments() {
    String message = underTest.getString("settings.copyright.2");

    assertThat(message).isEqualTo("Version {0}");
  }

  @Test
  void shouldReturnCorrectMessageWithArguments() {
    String message = underTest.getString("settings.copyright.2", "XXX");

    assertThat(message).isEqualTo("Version XXX");
  }
}