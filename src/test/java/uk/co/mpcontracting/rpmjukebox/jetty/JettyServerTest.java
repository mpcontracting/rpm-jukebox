package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.manager.ApplicationManager;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.net.BindException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JettyServerTest implements Constants {

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private RpmJukebox mockRpmJukebox;

    @Mock
    private ApplicationManager mockApplicationManager;

    @Mock
    private MessageManager mockMessageManager;

    @Mock
    private Server mockServer;

    @Mock
    private ServerConnector mockServerConnector;

    private JettyServer spyJettyServer;

    @Before
    public void setup() {
        spyJettyServer = spy(new JettyServer(mockAppProperties, mockRpmJukebox, mockApplicationManager, mockMessageManager));

        doReturn(mockServer).when(spyJettyServer).constructServer();
        doReturn(mockServerConnector).when(spyJettyServer).constructServerConnector(mockServer);

        when(mockMessageManager.getMessage(MESSAGE_SPLASH_INITIALISING_CACHE)).thenReturn("InitialiseCache");
        when(mockMessageManager.getMessage(MESSAGE_SPLASH_ALREADY_RUNNING)).thenReturn("AlreadyRunning");
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseJettyServer() {
        spyJettyServer.initialise();

        verify(mockRpmJukebox, times(1)).updateSplashProgress("InitialiseCache");
        verify(mockServer, times(1)).start();
    }

    @Test
    @SneakyThrows
    public void shouldShutdownApplicationIfBindExceptionThrownOnServerStart() {
        doThrow(new BindException("JettyServerTest.shouldShutdownApplicationIfBindExceptionThrownOnServerStart()"))
                .when(mockServer).start();

        spyJettyServer.initialise();

        verify(mockRpmJukebox, times(1)).updateSplashProgress("AlreadyRunning");
        verify(mockApplicationManager, times(1)).shutdown();
    }

    @Test
    @SneakyThrows
    public void shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart() {
        doThrow(new IllegalStateException("JettyServerTest.shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart()"))
                .when(mockServer).start();

        assertThatThrownBy(() -> spyJettyServer.initialise()).isInstanceOf(IllegalStateException.class);

        verify(mockApplicationManager, never()).shutdown();
    }

    @Test
    @SneakyThrows
    public void shouldStopJettyServer() {
        spyJettyServer.initialise();
        spyJettyServer.stop();

        verify(mockServer, times(1)).stop();
        verify(mockServer, times(1)).join();
    }

    @Test
    @SneakyThrows
    public void shouldStopJettyServerWhenServerIsNull() {
        spyJettyServer.stop();

        verify(mockServer, never()).stop();
        verify(mockServer, never()).join();
    }
}
