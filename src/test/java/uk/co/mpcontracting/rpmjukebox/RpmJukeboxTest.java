package uk.co.mpcontracting.rpmjukebox;

import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.component.ProgressSplashScreen;
import uk.co.mpcontracting.rpmjukebox.service.ApplicationLifecycleService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

import java.io.File;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RpmJukeboxTest extends AbstractGuiTest {

  @Autowired
  private RpmJukebox rpmJukebox;

  @Mock
  private ProgressSplashScreen splashScreen;

  @Mock
  private ConfigurableApplicationContext applicationContext;

  @Mock
  private ApplicationLifecycleService applicationLifecycleService;

  @BeforeEach
  public void beforeEach() {
    when(applicationContext.getBean(ApplicationLifecycleService.class)).thenReturn(applicationLifecycleService);
  }

  @Test
  public void shouldRunMain() {
    assertThatThrownBy(() -> RpmJukebox.main(null)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void shouldRunMainWhereLoggingFileAlreadyExists() throws Exception {
    File loggingFile = new File(RpmJukebox.getConfigDirectory(), "logback.xml");
    loggingFile.createNewFile();

    assertThatThrownBy(() -> RpmJukebox.main(null)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void shouldReturnEmptyListForDefaultIcons() {
    Collection<Image> defaultIcons = rpmJukebox.loadDefaultIcons();

    assertThat(defaultIcons).isEmpty();
  }

  @Test
  public void shouldCallStartOnApplicationManagerInBeforeInitialView() {
    rpmJukebox.beforeInitialView(null, applicationContext);

    verify(applicationLifecycleService).start(any());
  }

  @Test
  public void shouldNotCallStartOnApplicationManagerInBeforeInitialViewWithNullContext() {
    rpmJukebox.beforeInitialView(null, null);

    verify(applicationLifecycleService, never()).start(any());
  }

  @Test
  public void shouldUpdateSplashProgress() throws Exception {
    ReflectionTestUtils.setField(rpmJukebox, "splashScreen", splashScreen);

    rpmJukebox.updateSplashProgress("Test message");

    // Wait for UI thread
    Thread.sleep(250);

    verify(splashScreen).updateProgress("Test message");
  }
}
