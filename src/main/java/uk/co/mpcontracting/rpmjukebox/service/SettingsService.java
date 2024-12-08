package uk.co.mpcontracting.rpmjukebox.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.util.OsType;

@Slf4j
@Service
public class SettingsService {

  @Getter
  private OsType osType;
}
