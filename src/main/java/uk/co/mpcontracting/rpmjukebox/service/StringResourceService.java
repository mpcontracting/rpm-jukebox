package uk.co.mpcontracting.rpmjukebox.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.I18N_MESSAGE_BUNDLE;

@Slf4j
@Service
public class StringResourceService {

  private ResourceBundle resourceBundle;

  @PostConstruct
  public void postConstruct() {
    log.info("Initialising {}", getClass().getSimpleName());

    resourceBundle = ResourceBundle.getBundle(I18N_MESSAGE_BUNDLE);
  }

  public String getString(String key, Object... arguments) {
    return MessageFormat.format(resourceBundle.getString(key), arguments);
  }
}