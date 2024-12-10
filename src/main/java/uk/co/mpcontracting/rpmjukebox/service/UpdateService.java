package uk.co.mpcontracting.rpmjukebox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService extends EventAwareObject {

  public void downloadNewVersion() {

  }
}
