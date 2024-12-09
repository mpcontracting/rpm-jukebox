package uk.co.mpcontracting.rpmjukebox.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

@ExtendWith(MockitoExtension.class)
class ThreadRunnerTest extends AbstractGuiTest {

  @Autowired
  private ThreadRunner underTest;

  private CountDownLatch countDownLatch;
  private String threadName;

  @BeforeEach
  void beforeEach() {
    countDownLatch = new CountDownLatch(1);
  }

  @Test
  @SneakyThrows
  void shouldRunOffThread() {
    underTest.run(() -> {
      threadName = Thread.currentThread().getName();
      countDownLatch.countDown();
    });

    countDownLatch.await();

    assertThat(threadName).doesNotContain("JavaFX");
  }
  @Test
  public void shouldRunOnGuiThread() throws Exception {
    underTest.runOnGui(() -> {
      threadName = Thread.currentThread().getName();
      countDownLatch.countDown();
    });

    countDownLatch.await();

    assertThat(threadName).contains("JavaFX");
  }

}