package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EqBand {
    private int band;
    private double value;
}
