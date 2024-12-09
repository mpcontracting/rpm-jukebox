package uk.co.mpcontracting.rpmjukebox.service;

import com.google.gson.Gson;
import com.igormaznitsa.commons.version.Version;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.util.OsType;
import java.net.URL;

@Slf4j
@Service
public class SettingsService {

  @Getter
  private OsType osType;

  @Getter
  private Version version;

  @Getter
  private SystemSettings systemSettings;

  @Getter
  private URL dataFile;

  @Getter
  private Gson gson;

  public void saveSystemSettings() {

  }
}
