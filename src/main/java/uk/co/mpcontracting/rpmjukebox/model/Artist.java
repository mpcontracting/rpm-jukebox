package uk.co.mpcontracting.rpmjukebox.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(of = "artistId")
public class Artist {
    private String artistId;
    private String artistName;
    private String artistImage;
    private String biography;
    private String members;
}
