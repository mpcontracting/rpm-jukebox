package uk.co.mpcontracting.rpmjukebox.manager;

import static java.util.Optional.*;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;

@Slf4j
@Component
public class InternetManager extends EventAwareObject {

    @Autowired
    private SettingsManager settingsManager;

    public HttpURLConnection openConnection(URL url) throws Exception {
        log.debug("Opening connection to - {}", url);

        SystemSettings systemSettings = settingsManager.getSystemSettings();

        if (systemSettings.getProxyHost() != null && systemSettings.getProxyPort() != null) {
            log.debug("Using proxy : Host - {}, Port - {}", systemSettings.getProxyHost(),
                systemSettings.getProxyPort());

            HttpURLConnection connection = (HttpURLConnection)url.openConnection(new Proxy(Proxy.Type.HTTP,
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

        return (HttpURLConnection)url.openConnection();
    }
}
