package uk.co.mpcontracting.rpmjukebox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class YearFilter {
    @Getter
    private String display;
    @Getter
    private String year;

    @Override
    public String toString() {
        return display;
    }
}
