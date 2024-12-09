package uk.co.mpcontracting.rpmjukebox.util;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_INVALID_BORDER;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_VALID_BORDER;

import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class ValidationHelperTest extends AbstractGuiTest {

  @Test
  void shouldReturnNullStringAsBlank() {
    String result = ValidationHelper.nullAsBlank((String) null);

    assertThat(result).isEqualTo("");
  }

  @Test
  void shouldReturnStringAsTrimmedString() {
    String result = ValidationHelper.nullAsBlank("test ");

    assertThat(result).isEqualTo("test");
  }

  @Test
  void shouldReturnNullIntegerAsBlank() {
    String result = ValidationHelper.nullAsBlank((Integer) null);

    assertThat(result).isEqualTo("");
  }

  @Test
  void shouldReturnIntegerAsString() {
    String result = ValidationHelper.nullAsBlank(123);

    assertThat(result).isEqualTo("123");
  }

  @Test
  void shouldReturnNullBooleanAsFalse() {
    boolean result = ValidationHelper.nullAsFalse(null);

    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnBooleanAsBoolean() {
    boolean result = ValidationHelper.nullAsFalse(true);

    assertThat(result).isTrue();
  }

  @Test
  void shouldValidateTextField() {
    TextField textField = new TextField("abc");

    boolean result = ValidationHelper.validateTextField(textField, true, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldNotValidateTextFieldRequiredWithNoValue() {
    TextField textField = new TextField();

    boolean result = ValidationHelper.validateTextField(textField, true, null, null);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldValidateTextFieldNotRequiredWithValue() {
    TextField textField = new TextField("abc");

    boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldValidateTextFieldNotRequiredWithNoValue() {
    TextField textField = new TextField();

    boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldValidateTextFieldNotRequiredWithNoMinOrMax() {
    TextField textField = new TextField("abc");

    boolean result = ValidationHelper.validateTextField(textField, true, null, null);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldNotValidateTextFieldWithInvalidMinLength() {
    TextField textField = new TextField("abc");

    boolean result = ValidationHelper.validateTextField(textField, true, 50, null);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldNotValidateTextFieldWithInvalidMaxLength() {
    TextField textField = new TextField("abcdefghijklm");

    boolean result = ValidationHelper.validateTextField(textField, true, null, 5);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldValidateIntegerField() {
    TextField textField = new TextField("123");

    boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldNotValidateIntegerFieldRequiredWithNoValue() {
    TextField textField = new TextField();

    boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldValidateIntegerFieldNotRequiredWithValue() {
    TextField textField = new TextField("123");

    boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldValidateIntegerFieldNotRequiredWithNoValue() {
    TextField textField = new TextField();

    boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldNotValidateIntegerFieldWithInvalidCharacters() {
    TextField textField = new TextField("abc");

    boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldValidateIntegerFieldNotRequiredWithNoMinOrMax() {
    TextField textField = new TextField("123");

    boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);

    assertThat(result).isTrue();
    assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
  }

  @Test
  void shouldNotValidateIntegerFieldWithInvalidMinRange() {
    TextField textField = new TextField("10");

    boolean result = ValidationHelper.validateIntegerField(textField, true, 50, null);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }

  @Test
  void shouldNotValidateIntegerFieldWithInvalidMaxRange() {
    TextField textField = new TextField("500");

    boolean result = ValidationHelper.validateIntegerField(textField, true, null, 200);

    assertThat(result).isFalse();
    assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
  }
}