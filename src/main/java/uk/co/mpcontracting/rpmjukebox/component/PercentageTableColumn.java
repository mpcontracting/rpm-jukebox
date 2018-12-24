package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableColumn;

public class PercentageTableColumn<S, T> extends TableColumn<S, T> {
    private final DoubleProperty percentageWidth = new SimpleDoubleProperty(1);

    public PercentageTableColumn() {
        tableViewProperty().addListener((observableValue, oldValue, newValue) -> {
            if (prefWidthProperty().isBound()) {
                prefWidthProperty().unbind();
            }

            prefWidthProperty().bind(newValue.widthProperty().multiply(percentageWidth));
        });
    }

    public final DoubleProperty percentageWidthProperty() {
        return percentageWidth;
    }

    public final double getPercentageWidth() {
        return percentageWidthProperty().get();
    }

    public final void setPercentageWidth(double value) throws IllegalArgumentException {
        if (value >= 0 && value <= 1) {
            percentageWidthProperty().set(value);
        } else {
            throw new IllegalArgumentException(
                String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1$s", value));
        }
    }
}
