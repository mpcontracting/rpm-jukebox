package uk.co.mpcontracting.rpmjukebox.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;

class ConfirmControllerTest extends AbstractGuiTest {

  @SpyBean
  private ConfirmView confirmView;

  @Autowired
  private ConfirmController underTest;

  @SneakyThrows
  @PostConstruct
  void postConstruct() {
    init(confirmView);
  }

  @BeforeEach
  void beforeEach() {
    doNothing().when(confirmView).close();
  }

  @Test
  void shouldClickOkButtonWithOkRunnable() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(runnable, null);

    clickOn("#okButton");

    verify(runnable).run();
    verify(confirmView).close();
  }

  @Test
  void shouldClickOkButtonWithoutOkRunnable() {
    underTest.setRunnables(null, null);

    clickOn("#okButton");

    verify(confirmView).close();
  }

  @Test
  void shouldPressEnterKeyOnFocusedOkButton() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(runnable, null);
    setOkFocused();

    press(KeyCode.ENTER);

    verify(runnable).run();
    verify(confirmView).close();
  }

  @Test
  void shouldPressNonEnterKeyOnFocusedOkButton() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(runnable, null);
    setOkFocused();

    press(KeyCode.A);

    verify(runnable, never()).run();
    verify(confirmView, never()).close();
  }

  @Test
  void shouldClickCancelButtonWithOkRunnable() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(null, runnable);

    clickOn("#cancelButton");

    verify(runnable).run();
    verify(confirmView).close();
  }

  @Test
  void shouldClickCancelButtonWithoutOkRunnable() {
    underTest.setRunnables(null, null);

    clickOn("#cancelButton");

    verify(confirmView).close();
  }

  @Test
  void shouldPressEnterKeyOnFocusedCancelButton() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(null, runnable);
    setOkFocused();

    press(KeyCode.TAB).press(KeyCode.ENTER);

    verify(runnable).run();
    verify(confirmView).close();
  }

  @Test
  void shouldPressNonEnterKeyOnFocusedCancelButton() {
    Runnable runnable = mock(Runnable.class);
    underTest.setRunnables(null, runnable);
    setOkFocused();

    press(KeyCode.TAB).press(KeyCode.A);

    verify(runnable, never()).run();
    verify(confirmView, never()).close();
  }

  @SneakyThrows
  private void setOkFocused() {
    Platform.runLater(() -> underTest.setOkFocused());

    WaitForAsyncUtils.waitForFxEvents();
  }
}