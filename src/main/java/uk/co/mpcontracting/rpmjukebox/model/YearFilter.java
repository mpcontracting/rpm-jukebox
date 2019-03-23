package uk.co.mpcontracting.rpmjukebox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YearFilter {
    private String display;
    private String year;

    @Override
    public String toString() {
        return display;
    }
}
