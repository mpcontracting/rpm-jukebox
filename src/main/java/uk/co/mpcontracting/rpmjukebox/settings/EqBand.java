package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class EqBand {
    @Getter
    private int band;
    @Getter
    private double value;
}
