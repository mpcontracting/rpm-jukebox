package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import javax.annotation.PostConstruct;
import java.net.BindException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JettyServer implements Constants {

    private final AppProperties appProperties;
    private final RpmJukebox rpmJukebox;
    private final ApplicationManager applicationManager;
    private final MessageManager messageManager;

    private Server server;

    @SneakyThrows
    @PostConstruct
    public void initialise() {
        log.info("Initialising JettyServer on port - {}", appProperties.getJettyPort());

        rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_INITIALISING_CACHE));

        server = constructServer();

        ServerConnector connector = constructServerConnector(server);
        connector.setPort(appProperties.getJettyPort());

        server.setConnectors(new Connector[] { connector });

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(CachingMediaProxyServlet.class, "/cache");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { context, new DefaultHandler() });

        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            if (e instanceof BindException) {
                rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_ALREADY_RUNNING));

                try {
                    Thread.sleep(5000);
                } catch (Exception e2) {
                    // Do nothing
                }

                applicationManager.shutdown();
            } else {
                throw e;
            }
        }
    }

    // Package level for testing purposes
    Server constructServer() {
        return new Server();
    }

    ServerConnector constructServerConnector(Server server) {
        return new ServerConnector(server);
    }

    public void stop() throws Exception {
        log.info("Stopping JettyServer");

        if (server != null) {
            server.stop();
            server.join();
        }
    }
}
