package uk.co.mpcontracting.rpmjukebox.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import javafx.scene.control.TextField;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class ValidationHelperTest extends AbstractTest implements Constants {

    @Test
    public void shouldReturnNullStringAsBlank() {
        String result = ValidationHelper.nullAsBlank((String)null);
        
        assertThat("Result should be ''", result, equalTo(""));
    }
    
    @Test
    public void shouldReturnStringAsTrimmedString() {
        String result = ValidationHelper.nullAsBlank("test ");
        
        assertThat("Result should be 'test '", result, equalTo("test"));
    }
    
    @Test
    public void shouldReturnNullIntegerAsBlank() {
        String result = ValidationHelper.nullAsBlank((Integer)null);
        
        assertThat("Result should be ''", result, equalTo(""));
    }
    
    @Test
    public void shouldReturnIntegerAsString() {
        String result = ValidationHelper.nullAsBlank(123);
        
        assertThat("Result should be '123'", result, equalTo("123"));
    }
    
    @Test
    public void shouldReturnNullBooleanAsFalse() {
        boolean result = ValidationHelper.nullAsFalse(null);
        
        assertThat("Result should be false", result, equalTo(false));
    }
    
    @Test
    public void shouldReturnBooleanAsBoolean() {
        boolean result = ValidationHelper.nullAsFalse(true);
        
        assertThat("Result should be true", result, equalTo(true));
    }
    
    @Test
    public void shouldValidateTextField() {
        TextField textField = new TextField("abc");
        
        boolean result = ValidationHelper.validateTextField(textField, true, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateTextFieldRequiredWithNoValue() {
        TextField textField = new TextField();
        
        boolean result = ValidationHelper.validateTextField(textField, true, null, null);
        
        assertThat("Result should be true", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldValidateTextFieldNotRequiredWithValue() {
        TextField textField = new TextField("abc");
        
        boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldValidateTextFieldNotRequiredWithNoValue() {
        TextField textField = new TextField();
        
        boolean result = ValidationHelper.validateTextField(textField, false, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }

    @Test
    public void shouldValidateTextFieldNotRequiredWithNoMinOrMax() {
        TextField textField = new TextField("abc");
        
        boolean result = ValidationHelper.validateTextField(textField, true, null, null);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateTextFieldWithInvalidMinLength() {
        TextField textField = new TextField("abc");
        
        boolean result = ValidationHelper.validateTextField(textField, true, 50, null);
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateTextFieldWithInvalidMaxLength() {
        TextField textField = new TextField("abcdefghijklm");
        
        boolean result = ValidationHelper.validateTextField(textField, true, null, 5);
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldValidateIntegerField() {
        TextField textField = new TextField("123");
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateIntegerFieldRequiredWithNoValue() {
        TextField textField = new TextField();
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);
        
        assertThat("Result should be true", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldValidateIntegerFieldNotRequiredWithValue() {
        TextField textField = new TextField("123");
        
        boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldValidateIntegerFieldNotRequiredWithNoValue() {
        TextField textField = new TextField();
        
        boolean result = ValidationHelper.validateIntegerField(textField, false, 1, 500);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateIntegerFieldWithInvalidCharacters() {
        TextField textField = new TextField("abc");
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, 1, 500);
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldValidateIntegerFieldNotRequiredWithNoMinOrMax() {
        TextField textField = new TextField("123");
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, null, null);
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Style should be '" + STYLE_VALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_VALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateIntegerFieldWithInvalidMinRange() {
        TextField textField = new TextField("10");
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, 50, null);
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
    
    @Test
    public void shouldNotValidateIntegerFieldWithInvalidMaxRange() {
        TextField textField = new TextField("500");
        
        boolean result = ValidationHelper.validateIntegerField(textField, true, null, 200);
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Style should be '" + STYLE_INVALID_BORDER + "'", textField.getStyle(), equalTo(STYLE_INVALID_BORDER));
    }
}
