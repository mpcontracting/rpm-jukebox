package uk.co.mpcontracting.rpmjukebox.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = {"artistId", "albumId", "trackId"})
public class Track implements Serializable {
	private static final long serialVersionUID = 55518786963702600L;
	
	@Getter private int artistId;
    @Getter private String artistName;
    @Getter private String artistImage;
    @Getter private int albumId;
    @Getter private String albumName;
    @Getter private String albumImage;
    @Getter private int year;
    @Getter private int trackId;
    @Getter private String trackName;
    @Getter private int number;
    @Getter private String location;
    @Getter private boolean isPreferred;
    @Getter private List<String> genres;
}
