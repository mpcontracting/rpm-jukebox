package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.scene.input.KeyCode;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class ConfirmControllerTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private ConfirmController underTest;

    @Autowired
    private ConfirmView originalConfirmView;

    private ConfirmView confirmView;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        confirmView = spy(originalConfirmView);

        setField(underTest, "confirmView", confirmView);

        init(confirmView);
    }

    @Before
    public void setup() {
        doNothing().when(confirmView).close();
    }

    @Test
    public void shouldClickOkButtonNoOkRunnable() {
        underTest.setRunnables(null, null);

        clickOn("#okButton");

        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldClickOkButtonWithOkRunnable() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(runnable, null);

        clickOn("#okButton");

        verify(runnable, times(1)).run();
        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldPressEnterKeyOnFocusedOkButton() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(runnable, null);
        setOkFocused();

        press(KeyCode.ENTER);

        verify(runnable, times(1)).run();
        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldPressNonEnterKeyOnFocusedOkButton() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(runnable, null);
        setOkFocused();

        press(KeyCode.A);

        verify(runnable, never()).run();
        verify(confirmView, never()).close();
    }

    @Test
    public void shouldClickCancelButtonNoOkRunnable() {
        underTest.setRunnables(null, null);

        clickOn("#cancelButton");

        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldClickCancelButtonWithOkRunnable() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(null, runnable);

        clickOn("#cancelButton");

        verify(runnable, times(1)).run();
        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldPressEnterKeyOnFocusedCancelButton() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(null, runnable);
        setOkFocused();

        press(KeyCode.TAB).press(KeyCode.ENTER);

        verify(runnable, times(1)).run();
        verify(confirmView, times(1)).close();
    }

    @Test
    public void shouldPressNonEnterKeyOnFocusedCancelButton() {
        Runnable runnable = mock(Runnable.class);
        underTest.setRunnables(null, runnable);
        setOkFocused();

        press(KeyCode.TAB).press(KeyCode.A);

        verify(runnable, never()).run();
        verify(confirmView, never()).close();
    }

    @SneakyThrows
    private void setOkFocused() {
        threadRunner.runOnGui(() -> underTest.setOkFocused());

        WaitForAsyncUtils.waitForFxEvents();
    }
}
