package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class EqBand {
	@Getter @Setter private int band;
	@Getter @Setter private double value;
}
