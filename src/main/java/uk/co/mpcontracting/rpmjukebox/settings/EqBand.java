package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EqBand {
    private int band;
    private double value;
}
