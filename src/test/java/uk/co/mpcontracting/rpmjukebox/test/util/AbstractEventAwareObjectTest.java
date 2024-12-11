package uk.co.mpcontracting.rpmjukebox.test.util;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.event.EventProcessor;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractEventAwareObjectTest {

  @Mock
  protected EventProcessor eventProcessor;

  private ApplicationContext originalContext;
  protected ApplicationContext applicationContext;

  @BeforeEach
  void abstractBeforeEach() {
    originalContext = getField(ContextHelper.class, "applicationContext", ApplicationContext.class);
    applicationContext = mock(ApplicationContext.class);
    setField(ContextHelper.class, "applicationContext", applicationContext);

    lenient().when(applicationContext.getBean(EventProcessor.class)).thenReturn(eventProcessor);
  }

  @AfterEach
  void abstractAfterEach() {
    setField(ContextHelper.class, "applicationContext", originalContext);
  }
}
