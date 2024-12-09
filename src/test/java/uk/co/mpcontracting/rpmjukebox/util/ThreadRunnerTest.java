package uk.co.mpcontracting.rpmjukebox.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThreadRunnerTest {

  private ThreadRunner underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new ThreadRunner(Executors.newCachedThreadPool());
  }

  @Test
  @SneakyThrows
  void shouldRunThread() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    AtomicReference<String> threadName = new AtomicReference<>();

    underTest.run(() -> {
      threadName.set(Thread.currentThread().getName());
      countDownLatch.countDown();
    });

    countDownLatch.await();

    assertThat(threadName.get()).doesNotContain("JavaFX");
  }
}