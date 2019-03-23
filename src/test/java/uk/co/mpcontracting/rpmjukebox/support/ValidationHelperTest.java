package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.control.TextField;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationHelperTest extends AbstractGUITest implements Constants {

    @Test
    public void shouldReturnNullStringAsBlank() {
        String result = ValidationHelper.nullAsBlank((String)null);

        assertThat(result).isEqualTo("");
    }

    @Test
    public void shouldReturnStringAsTrimmedString() {
        String result = ValidationHelper.nullAsBlank("test ");

        assertThat(result).isEqualTo("test");
    }

    @Test
    public void shouldReturnNullIntegerAsBlank() {
        String result = ValidationHelper.nullAsBlank((Integer)null);

        assertThat(result).isEqualTo("");
    }

    @Test
    public void shouldReturnIntegerAsString() {
        String result = ValidationHelper.nullAsBlank(123);

        assertThat(result).isEqualTo("123");
    }

    @Test
    public void shouldReturnNullBooleanAsFalse() {
        boolean result = ValidationHelper.nullAsFalse(null);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnBooleanAsBoolean() {
        boolean result = ValidationHelper.nullAsFalse(true);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateTextField() {
        TextField textField = new TextField("abc");

        boolean result = ValidationHelper.validateTextField(textField, true, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldNotValidateTextFieldRequiredWithNoValue() {
        TextField textField = new TextField();

        boolean result = ValidationHelper.validateTextField(textField, true, null, null);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldValidateTextFieldNotRequiredWithValue() {
        TextField textField = new TextField("abc");

        boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldValidateTextFieldNotRequiredWithNoValue() {
        TextField textField = new TextField();

        boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldValidateTextFieldNotRequiredWithNoMinOrMax() {
        TextField textField = new TextField("abc");

        boolean result = ValidationHelper.validateTextField(textField, true, null, null);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldNotValidateTextFieldWithInvalidMinLength() {
        TextField textField = new TextField("abc");

        boolean result = ValidationHelper.validateTextField(textField, true, 50, null);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldNotValidateTextFieldWithInvalidMaxLength() {
        TextField textField = new TextField("abcdefghijklm");

        boolean result = ValidationHelper.validateTextField(textField, true, null, 5);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldValidateIntegerField() {
        TextField textField = new TextField("123");

        boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldNotValidateIntegerFieldRequiredWithNoValue() {
        TextField textField = new TextField();

        boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldValidateIntegerFieldNotRequiredWithValue() {
        TextField textField = new TextField("123");

        boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldValidateIntegerFieldNotRequiredWithNoValue() {
        TextField textField = new TextField();

        boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldNotValidateIntegerFieldWithInvalidCharacters() {
        TextField textField = new TextField("abc");

        boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldValidateIntegerFieldNotRequiredWithNoMinOrMax() {
        TextField textField = new TextField("123");

        boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);

        assertThat(result).isTrue();
        assertThat(textField.getStyle()).isEqualTo(STYLE_VALID_BORDER);
    }

    @Test
    public void shouldNotValidateIntegerFieldWithInvalidMinRange() {
        TextField textField = new TextField("10");

        boolean result = ValidationHelper.validateIntegerField(textField, true, 50, null);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }

    @Test
    public void shouldNotValidateIntegerFieldWithInvalidMaxRange() {
        TextField textField = new TextField("500");

        boolean result = ValidationHelper.validateIntegerField(textField, true, null, 200);

        assertThat(result).isFalse();
        assertThat(textField.getStyle()).isEqualTo(STYLE_INVALID_BORDER);
    }
}
