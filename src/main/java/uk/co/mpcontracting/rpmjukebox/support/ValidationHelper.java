package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.control.TextField;

public abstract class ValidationHelper implements Constants {

    private ValidationHelper() {
    }

    public static String nullAsBlank(String string) {
        if (string == null) {
            return "";
        }

        return string.trim();
    }

    public static String nullAsBlank(Integer integer) {
        if (integer == null) {
            return "";
        }

        return integer.toString();
    }

    public static boolean nullAsFalse(Boolean bool) {
        if (bool == null) {
            return false;
        }

        return bool;
    }

    public static boolean validateTextField(TextField textField, boolean required, Integer minLength,
        Integer maxLength) {
        boolean isValid = false;

        if (!required && textField.getText().isEmpty()) {
            isValid = true;
        } else {
            int length = textField.getText().length();

            boolean validLength = true;

            if (required && length < 1) {
                validLength = false;
            }

            if (minLength != null && length < minLength) {
                validLength = false;
            }

            if (maxLength != null && length > maxLength) {
                validLength = false;
            }

            isValid = validLength;
        }

        if (!isValid) {
            textField.setStyle(STYLE_INVALID_BORDER);
        } else {
            textField.setStyle(STYLE_VALID_BORDER);
        }

        return isValid;
    }

    public static boolean validateIntegerField(TextField textField, boolean required, Integer minValue,
        Integer maxValue) {
        boolean isValid = false;

        if (!required && textField.getText().isEmpty()) {
            isValid = true;
        } else {
            String text = textField.getText();

            if (text.matches("\\d+")) {
                if (minValue == null && maxValue == null) {
                    isValid = true;
                } else {
                    int value = Integer.parseInt(text);
                    boolean validRange = true;

                    if (minValue != null && value < minValue) {
                        validRange = false;
                    }

                    if (maxValue != null && value > maxValue) {
                        validRange = false;
                    }

                    isValid = validRange;
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
}
