package uk.co.mpcontracting.rpmjukebox.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = "artistId")
public class Artist {
    @Getter
    private String artistId;
    @Getter
    private String artistName;
    @Getter
    private String artistImage;
    @Getter
    private String biography;
    @Getter
    private String members;
}
