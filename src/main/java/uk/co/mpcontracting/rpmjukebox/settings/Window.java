package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Window {
    private double x;
    private double y;
    private double width;
    private double height;
}
