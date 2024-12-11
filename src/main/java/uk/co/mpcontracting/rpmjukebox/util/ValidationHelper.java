package uk.co.mpcontracting.rpmjukebox.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_INVALID_BORDER;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_VALID_BORDER;

import javafx.scene.control.TextField;

public final class ValidationHelper {

  private ValidationHelper() {}

  public static String nullAsBlank(String string) {
    if (isNull(string)) {
      return "";
    }

    return string.trim();
  }

  public static String nullAsBlank(Integer integer) {
    if (isNull(integer)) {
      return "";
    }

    return integer.toString();
  }

  public static boolean nullAsFalse(Boolean bool) {
    if (isNull(bool)) {
      return false;
    }

    return bool;
  }

  public static boolean validateTextField(TextField textField, boolean required, Integer minLength, Integer maxLength) {
    boolean isValid;

    if (!required && textField.getText().isEmpty()) {
      isValid = true;
    } else {
      int length = textField.getText().length();

      boolean validLength = !required || length >= 1;

      isValid = validLength && isValidRange(minLength, maxLength, length);
    }

    if (!isValid) {
      textField.setStyle(STYLE_INVALID_BORDER);
    } else {
      textField.setStyle(STYLE_VALID_BORDER);
    }

    return isValid;
  }

  public static boolean validateIntegerField(TextField textField, boolean required, Integer minValue, Integer maxValue) {
    boolean isValid = false;

    if (!required && textField.getText().isEmpty()) {
      isValid = true;
    } else {
      String text = textField.getText();

      if (text.matches("\\d+")) {
        if (isNull(minValue) && isNull(maxValue)) {
          isValid = true;
        } else {
          isValid = isValidRange(minValue, maxValue, Integer.parseInt(text));
        }
      }
    }

    if (!isValid) {
      textField.setStyle(STYLE_INVALID_BORDER);
    } else {
      textField.setStyle(STYLE_VALID_BORDER);
    }

    return isValid;
  }

  private static boolean isValidRange(Integer minValue, Integer maxValue, int value) {
    boolean validRange = isNull(minValue) || value >= minValue;

    if (nonNull(maxValue) && value > maxValue) {
      validRange = false;
    }

    return validRange;
  }
}
