package uk.co.mpcontracting.rpmjukebox.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class JettyServer implements InitializingBean, Constants {
	
	@Autowired
	private SettingsManager settingsManager;
	
	private Server server;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising JettyServer");
		
		server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(settingsManager.getPropertyInteger(PROP_INTERNAL_JETTY_PORT));
        
        server.setConnectors(new Connector[] {connector});
        
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(CachingMediaProxyServlet.class, "/cache");
        
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {context, new DefaultHandler()});
        
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
