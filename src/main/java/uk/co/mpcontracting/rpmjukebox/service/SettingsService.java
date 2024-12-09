package uk.co.mpcontracting.rpmjukebox.service;

import com.google.gson.Gson;
import com.igormaznitsa.commons.version.Version;
import java.net.URL;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.util.OsType;

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

  void loadWindowSettings(Stage stage) {

  }

  void saveWindowSettings(Stage stage) {

  }

  void loadSystemSettings() {

  }

  public void saveSystemSettings() {

  }

  void loadUserSettings() {

  }

  void saveUserSettings() {

  }
}
