package uk.co.mpcontracting.rpmjukebox.jetty;

import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_ALREADY_RUNNING;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_INITIALISING_CACHE;

import jakarta.annotation.PostConstruct;
import java.net.BindException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.service.ApplicationLifecycleService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JettyServer extends EventAwareObject {

  private final RpmJukebox rpmJukebox;
  private final ApplicationProperties applicationProperties;
  private final StringResourceService stringResourceService;

  @Lazy
  @Autowired
  private ApplicationLifecycleService applicationLifecycleService;

  private Server server;

  @SneakyThrows
  @PostConstruct
  public void initialise() {
    log.info("Initialising JettyServer on port - {}", applicationProperties.getJettyPort());

    rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_INITIALISING_CACHE));

    server = constructServer(applicationProperties.getJettyPort());
    server.setHandler(constructServletContextHandler());

    try {
      server.start();
    } catch (Exception e) {
      if (e instanceof BindException) {
        rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_ALREADY_RUNNING));

        try {
          Thread.sleep(5000);
        } catch (Exception e2) {
          // Do nothing
        }

        applicationLifecycleService.shutdown();
      } else {
        throw e;
      }
    }
  }

  protected Server constructServer(int port) {
    return new Server(port);
  }

  protected ServletContextHandler constructServletContextHandler() {
    ServletContextHandler servletContextHandler = new ServletContextHandler("/");
    servletContextHandler.addServlet(CachingMediaProxyServlet.class, "/cache");

    return servletContextHandler;
  }

  public void stop() throws Exception {
    log.info("Stopping JettyServer");

    if (server != null) {
      server.stop();
      server.join();
    }
  }
}