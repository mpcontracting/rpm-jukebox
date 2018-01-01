package uk.co.mpcontracting.rpmjukebox.jetty;

import java.net.BindException;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class JettyServer implements Constants {

    @Autowired
    private RpmJukebox rpmJukebox;

    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private MessageManager messageManager;

    @Value("${internal.jetty.port}")
    private int internalJettyPort;

    private Server server;

    @SneakyThrows
    @PostConstruct
    public void initialise() {
        log.info("Initialising JettyServer on port - {}", internalJettyPort);

        rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_INITIALISING_CACHE));

        server = constructServer();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(internalJettyPort);

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

    public void stop() throws Exception {
        log.info("Stopping JettyServer");

        if (server != null) {
            server.stop();
            server.join();
        }
    }
}
