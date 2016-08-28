package uk.co.mpcontracting.rpmjukebox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString(includeFieldNames = true)
public class SystemSettings {
	@Getter private double defaultVolume;
	@Getter private int maxSearchHits;
	@Getter private int maxPlaylistSize;
	@Getter private int randomPlaylistSize;
	@Getter private int cacheSizeMb;
}
