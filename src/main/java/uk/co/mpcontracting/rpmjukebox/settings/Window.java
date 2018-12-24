package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class Window {
    @Getter
    private double x;
    @Getter
    private double y;
    @Getter
    private double width;
    @Getter
    private double height;
}
