package uk.co.mpcontracting.rpmjukebox.controller;

import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.scene.input.KeyCode;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;

public class ConfirmControllerTest extends AbstractTest {

    @Autowired
    private ConfirmController confirmController;
    
    @Autowired
    private ConfirmView confirmView;

    private ConfirmView spyConfirmView;

    @PostConstruct
    public void constructView() throws Exception {
        spyConfirmView = spy(confirmView);
        
        ReflectionTestUtils.setField(confirmController, "confirmView", spyConfirmView);
        
        init(spyConfirmView);
    }
    
    @Before
    public void setup() {
        doNothing().when(spyConfirmView).close();
    }

    @Test
    public void shouldClickOkButtonNoOkRunnable() {
        confirmController.setRunnables(null, null);
        
        clickOn("#okButton");
        
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldClickOkButtonWithOkRunnable() {
        Runnable mockOkRunnable = mock(Runnable.class);
        confirmController.setRunnables(mockOkRunnable, null);
        
        clickOn("#okButton");
        
        verify(mockOkRunnable, times(1)).run();
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldPressEnterKeyOnFocusedOkButton() throws Exception {
        Runnable mockOkRunnable = mock(Runnable.class);
        confirmController.setRunnables(mockOkRunnable, null);
        setOkFocused();
        
        press(KeyCode.ENTER);
        
        verify(mockOkRunnable, times(1)).run();
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldPressNonEnterKeyOnFocusedOkButton() throws Exception {
        Runnable mockOkRunnable = mock(Runnable.class);
        confirmController.setRunnables(mockOkRunnable, null);
        setOkFocused();
        
        press(KeyCode.A);
        
        verify(mockOkRunnable, never()).run();
        verify(spyConfirmView, never()).close();
    }
    
    @Test
    public void shouldClickCancelButtonNoOkRunnable() {
        confirmController.setRunnables(null, null);
        
        clickOn("#cancelButton");
        
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldClickCancelButtonWithOkRunnable() {
        Runnable mockCancelRunnable = mock(Runnable.class);
        confirmController.setRunnables(null, mockCancelRunnable);
        
        clickOn("#cancelButton");
        
        verify(mockCancelRunnable, times(1)).run();
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldPressEnterKeyOnFocusedCancelButton() throws Exception {
        Runnable mockCancelRunnable = mock(Runnable.class);
        confirmController.setRunnables(null, mockCancelRunnable);
        setOkFocused();
        
        press(KeyCode.TAB).press(KeyCode.ENTER);

        verify(mockCancelRunnable, times(1)).run();
        verify(spyConfirmView, times(1)).close();
    }
    
    @Test
    public void shouldPressNonEnterKeyOnFocusedCancelButton() throws Exception {
        Runnable mockCancelRunnable = mock(Runnable.class);
        confirmController.setRunnables(null, mockCancelRunnable);
        setOkFocused();
        
        press(KeyCode.TAB).press(KeyCode.A);
        
        verify(mockCancelRunnable, never()).run();
        verify(spyConfirmView, never()).close();
    }
    
    private void setOkFocused() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        ThreadRunner.runOnGui(() -> {
            confirmController.setOkFocused();
            latch.countDown();
        });
        
        latch.await(2000, TimeUnit.MILLISECONDS);
    }
}
