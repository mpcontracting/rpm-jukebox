package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;

@ToString(includeFieldNames = true)
public class Settings {
    @Getter
    @Setter
    private boolean shuffle;
    @Getter
    @Setter
    private Repeat repeat;
    @Getter
    @Setter
    private SystemSettings systemSettings;
    @Getter
    @Setter
    private List<EqBand> eqBands;
    @Getter
    @Setter
    private List<PlaylistSettings> playlists;
}
