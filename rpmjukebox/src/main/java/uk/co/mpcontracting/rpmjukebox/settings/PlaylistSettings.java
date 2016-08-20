package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString(includeFieldNames = true)
public class PlaylistSettings {
	@Getter @Setter private int id;
	@Getter @Setter private String name;
	@Getter @Setter private List<String> tracks;
	
	public PlaylistSettings(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
