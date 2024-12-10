package uk.co.mpcontracting.rpmjukebox.jetty;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_ALREADY_RUNNING;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_INITIALISING_CACHE;

import java.net.BindException;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.service.ApplicationLifecycleService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;

@ExtendWith(MockitoExtension.class)
class JettyServerTest {

  private static final String MESSAGE_INITIALISING_CACHE = "InitialiseCache";
  private static final String MESSAGE_ALREADY_RUNNING = "AlreadyRunning";

  @Mock
  private RpmJukebox rpmJukebox;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private StringResourceService stringResourceService;

  @Mock
  private ApplicationLifecycleService applicationLifecycleService;

  @Mock
  private Server server;

  @Mock
  private ServerConnector serverConnector;

  private JettyServer underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new JettyServer(rpmJukebox, applicationProperties, stringResourceService));

    setField(underTest, "applicationLifecycleService", applicationLifecycleService);

    lenient().doReturn(server).when(underTest).constructServer();
    lenient().doReturn(serverConnector).when(underTest).constructServerConnector(server);

    lenient().when(stringResourceService.getString(MESSAGE_SPLASH_INITIALISING_CACHE)).thenReturn(MESSAGE_INITIALISING_CACHE);
    lenient().when(stringResourceService.getString(MESSAGE_SPLASH_ALREADY_RUNNING)).thenReturn(MESSAGE_ALREADY_RUNNING);
  }

  @Test
  @SneakyThrows
  void shouldInitialiseJettyServer() {
    int jettyPort = getFaker().number().numberBetween(8080, 9999);

    when(applicationProperties.getJettyPort()).thenReturn(jettyPort);

    underTest.initialise();

    verify(rpmJukebox).updateSplashProgress(MESSAGE_INITIALISING_CACHE);
    verify(serverConnector).setPort(jettyPort);
    verify(server).setHandler(any(Handler.class));
    verify(server).start();
  }

  @Test
  @SneakyThrows
  void shouldShutdownApplicationIfBindExceptionThrownOnServerStart() {
    doThrow(new BindException("JettyServerTest.shouldShutdownApplicationIfBindExceptionThrownOnServerStart()"))
        .when(server).start();

    underTest.initialise();

    verify(rpmJukebox).updateSplashProgress(MESSAGE_ALREADY_RUNNING);
    verify(applicationLifecycleService).shutdown();
  }

  @Test
  @SneakyThrows
  void shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart() {
    doThrow(new IllegalStateException("JettyServerTest.shouldRethrowExceptionIfNonBindExceptionThrownOnServerStart()"))
        .when(server).start();

    assertThatThrownBy(() -> underTest.initialise()).isInstanceOf(IllegalStateException.class);

    verify(rpmJukebox, never()).updateSplashProgress(MESSAGE_ALREADY_RUNNING);
    verify(applicationLifecycleService, never()).shutdown();
  }

  @Test
  @SneakyThrows
  void shouldStopJettyServer() {
    underTest.initialise();
    underTest.stop();

    verify(server).stop();
    verify(server).join();
  }

  @Test
  @SneakyThrows
  void shouldStopJettyServerWhenServerIsNull() {
    underTest.stop();

    verify(server, never()).stop();
    verify(server, never()).join();
  }
}