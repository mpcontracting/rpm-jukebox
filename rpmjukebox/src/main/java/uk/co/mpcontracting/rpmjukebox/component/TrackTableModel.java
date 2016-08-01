package uk.co.mpcontracting.rpmjukebox.component;

import java.util.List;

import lombok.Getter;

public class TrackTableModel {
	@Getter private boolean isPreferred;
	@Getter private int trackNumber;
	@Getter private String trackName;
	@Getter private String artistName;
	@Getter private int albumYear;
	@Getter private String albumName;
	@Getter private List<String> genres;
}
