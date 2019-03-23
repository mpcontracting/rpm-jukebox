package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;

import java.util.List;

@Getter
@Setter
@ToString
public class Settings {
    private boolean shuffle;
    private Repeat repeat;
    private List<EqBand> eqBands;
    private List<PlaylistSettings> playlists;
}
