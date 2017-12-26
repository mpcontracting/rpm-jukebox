package uk.co.mpcontracting.rpmjukebox.manager;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;

@Slf4j
@Component
public class InternetManager extends EventAwareObject {

    @Autowired
    private SettingsManager settingsManager;

    @Value("${http.timeout}")
    private int httpTimeout;

    @Value("${http.retry}")
    private boolean httpRetry;

    @Value("${http.pool.size}")
    private int httpPoolSize;

    @Value("${http.keep.alive}")
    private int httpKeepAlive;

    private OkHttpClient httpClient;

    @Synchronized
    private void lazyInitialiseClient() {
        if (httpClient == null) {
            initialiseClient();
        }
    }

    @Synchronized
    private void initialiseClient() {
        log.debug("Initialising HTTP client");

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().connectTimeout(httpTimeout, TimeUnit.SECONDS)
            .readTimeout(httpTimeout, TimeUnit.SECONDS).writeTimeout(httpTimeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(httpRetry)
            .connectionPool(new ConnectionPool(httpPoolSize, httpKeepAlive, TimeUnit.SECONDS));

        SystemSettings systemSettings = settingsManager.getSystemSettings();

        if (systemSettings.getProxyHost() != null && systemSettings.getProxyPort() != null) {
            log.debug(
                "Using proxy : Host - " + systemSettings.getProxyHost() + ", Port - " + systemSettings.getProxyPort());

            clientBuilder = clientBuilder.proxy(new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(systemSettings.getProxyHost(), systemSettings.getProxyPort())));

            if (systemSettings.getProxyRequiresAuthentication() != null
                && systemSettings.getProxyRequiresAuthentication()) {
                log.debug("Using proxy authentication for user - " + systemSettings.getProxyUsername());

                clientBuilder = clientBuilder.proxyAuthenticator((route, response) -> {
                    return response.request().newBuilder()
                        .header("Proxy-Authorization",
                            Credentials.basic(systemSettings.getProxyUsername(), systemSettings.getProxyPassword()))
                        .build();
                });
            }
        }

        httpClient = clientBuilder.build();
    }

    public Response openConnection(URL url) throws Exception {
        if (httpClient == null) {
            lazyInitialiseClient();
        }

        log.debug("Opening connection to - " + url);

        return httpClient.newCall(new Request.Builder().url(url).get().build()).execute();
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            case SETTINGS_UPDATED: {
                initialiseClient();

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
