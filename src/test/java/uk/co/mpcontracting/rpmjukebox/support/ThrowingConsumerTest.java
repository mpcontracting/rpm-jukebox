package uk.co.mpcontracting.rpmjukebox.support;

import lombok.Builder;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThrowingConsumerTest {

    @Test
    public void shouldNotThrowARuntimeException() {
        assertThatCode(() -> {
            TestClass testClass = TestClass.builder().test("Test").build();
            Optional.of(testClass).ifPresent((ThrowingConsumer<TestClass>) tc -> tc.test().length());
        }).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowARuntimeException() {
        assertThatThrownBy(() -> {
            TestClass testClass = TestClass.builder().build();
            Optional.of(testClass).ifPresent((ThrowingConsumer<TestClass>) tc -> tc.test().length());
        }).isInstanceOf(RuntimeException.class);
    }

    @Builder
    private record TestClass(String test) {}
}