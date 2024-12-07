package uk.co.mpcontracting.rpmjukebox.component;

import de.felixroske.jfxsupport.SplashScreen;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProgressSplashScreen extends SplashScreen {

  public void updateProgress(String message) {
    log.debug("Updating progress - {}", message);
  }
}
