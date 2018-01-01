package uk.co.mpcontracting.rpmjukebox.jetty;

import static org.mockito.Mockito.*;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class JettyServerTest extends AbstractTest {

    @Autowired
    private JettyServer jettyServer;

    @Mock
    private ApplicationManager mockApplicationManager;
    
    @Mock
    private Server mockServer;

    private JettyServer spyJettyServer;

    @Before
    public void setup() {
        spyJettyServer = spy(jettyServer);
        ReflectionTestUtils.setField(spyJettyServer, "applicationManager", mockApplicationManager);
        ReflectionTestUtils.setField(spyJettyServer, "server", mockServer);
    }

    @Test
    public void shouldStopJettyServer() throws Exception {
        spyJettyServer.stop();

        verify(mockServer, times(1)).stop();
        verify(mockServer, times(1)).join();
    }

    @Test
    public void shouldStopJettyServerWhenServerIsNull() throws Exception {
        ReflectionTestUtils.setField(spyJettyServer, "server", null);

        spyJettyServer.stop();
    }
    
    @Test
    public void shouldNotInitialiseIfAlreadyInitialised() {
        spyJettyServer.initialise();
        
        verify(mockApplicationManager, times(1)).shutdown();
    }
}
