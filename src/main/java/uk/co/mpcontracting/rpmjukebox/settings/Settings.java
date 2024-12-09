package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;

@Data
@Builder
public class Settings {

  private boolean shuffle;
  private Repeat repeat;
  private List<EqBand> eqBands;
  private List<PlaylistSettings> playlists;
}