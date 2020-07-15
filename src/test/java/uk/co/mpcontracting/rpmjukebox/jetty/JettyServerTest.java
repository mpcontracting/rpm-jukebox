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
    private AppProperties appProperties;

    @Mock
    private RpmJukebox rpmJukebox;

    @Mock
    private ApplicationManager applicationManager;

    @Mock
    private MessageManager messageManager;

    @Mock
    private Server server;

    @Mock
    private ServerConnector serverConnector;

    private JettyServer underTest;

    @Before
    public void setup() {
        underTest = spy(new JettyServer(appProperties, rpmJukebox, messageManager));
        underTest.wireApplicationManager(applicationManager);

        doReturn(server).when(underTest).constructServer();
        doReturn(serverConnector).when(underTest).constructServerConnector(server);

        when(messageManager.getMessage(MESSAGE_SPLASH_INITIALISING_CACHE)).thenReturn("InitialiseCache");
        when(messageManager.getMessage(MESSAGE_SPLASH_ALREADY_RUNNING)).thenReturn("AlreadyRunning");
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseJettyServer() {
        underTest.initialise();

        verify(rpmJukebox, times(1)).updateSplashProgress("InitialiseCache");
        verify(server, times(1)).start();
    }

    @Test
    @SneakyThrows
    public void shouldShutdownApplicationIfBindExceptionThrownOnServerStart() {
        doThrow(new BindException("JettyServerTest.shouldShutdownApplicationIfBindExceptionThrownOnServerStart()"))
                .when(server).start();

        underTest.initialise();

        verify(rpmJukebox, times(1)).updateSplashProgress("AlreadyRunning");
        verify(applicationManager, times(1)).shutdown();
    }

    @Test
    @SneakyThrows
    public void shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart() {
        doThrow(new IllegalStateException("JettyServerTest.shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart()"))
                .when(server).start();

        assertThatThrownBy(() -> underTest.initialise()).isInstanceOf(IllegalStateException.class);

        verify(applicationManager, never()).shutdown();
    }

    @Test
    @SneakyThrows
    public void shouldStopJettyServer() {
        underTest.initialise();
        underTest.stop();

        verify(server, times(1)).stop();
        verify(server, times(1)).join();
    }

    @Test
    @SneakyThrows
    public void shouldStopJettyServerWhenServerIsNull() {
        underTest.stop();

        verify(server, never()).stop();
        verify(server, never()).join();
    }
}
