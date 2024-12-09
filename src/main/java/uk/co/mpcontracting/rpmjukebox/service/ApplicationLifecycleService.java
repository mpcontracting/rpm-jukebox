package uk.co.mpcontracting.rpmjukebox.service;

import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationLifecycleService extends EventAwareObject {

  @SneakyThrows
  public void start(Stage stage) {
    log.info("Starting application");

    // TODO : Loop through all AbstractModalViews and initialise them
  }

  public void stop() {
    log.info("Stopping application");
  }

  public void shutdown() {

  }
}
