package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternetService {

  private final SettingsService settingsService;

  public URLConnection openConnection(URL url) throws IOException {
    log.debug("Opening connection to - {}", url);

    if (!"file".equals(url.getProtocol())) {
      SystemSettings systemSettings = settingsService.getSystemSettings();

      if (nonNull(systemSettings.getProxyHost()) && nonNull(systemSettings.getProxyPort())) {
        log.debug("Using proxy : Host - {}, Port - {}", systemSettings.getProxyHost(), systemSettings.getProxyPort());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
            new InetSocketAddress(systemSettings.getProxyHost(), systemSettings.getProxyPort())));

        ofNullable(systemSettings.getProxyRequiresAuthentication()).ifPresent(requiresAuthentication -> {
          if (requiresAuthentication) {
            log.debug("Using proxy authentication for user - {}", systemSettings.getProxyUsername());

            String authorization = systemSettings.getProxyUsername() + ":" + systemSettings.getProxyPassword();
            String authToken = Base64.getEncoder().encodeToString(authorization.getBytes());

            connection.setRequestProperty("Proxy-Authorization", "Basic " + authToken);
          }
        });

        return connection;
      }
    }

    return url.openConnection();
  }
}
