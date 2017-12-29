package uk.co.mpcontracting.rpmjukebox.jetty;

import javax.annotation.PostConstruct;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class JettyServer implements Constants {

    @Value("${internal.jetty.port}")
    private int internalJettyPort;

    private Server server;

    @SneakyThrows
    @PostConstruct
    public void initialise() {
        log.info("Initialising JettyServer on port - {}", internalJettyPort);

        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(internalJettyPort);

        server.setConnectors(new Connector[] { connector });

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(CachingMediaProxyServlet.class, "/cache");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { context, new DefaultHandler() });

        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        log.info("Stopping JettyServer");

        if (server != null) {
            server.stop();
            server.join();
        }
    }
}
