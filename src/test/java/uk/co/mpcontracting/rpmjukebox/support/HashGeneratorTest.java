package uk.co.mpcontracting.rpmjukebox.support;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(MockitoJUnitRunner.class)
public class HashGeneratorTest {

    private HashGenerator hashGenerator;

    @Before
    public void setup() {
        hashGenerator = new HashGenerator();
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWithNullHashKeys() {
        assertThatThrownBy(() -> hashGenerator.generateHash((Object[]) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWithNoHashKeys() {
        assertThatThrownBy(() -> hashGenerator.generateHash())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionIfKeyLengthIsZero() {
        assertThatThrownBy(() -> hashGenerator.generateHash("", "")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGenerateAHashFromASingleObject() {
        String hash = hashGenerator.generateHash("Object 1");

        assertThat(hash).isEqualTo("9c98295d0c3d33bf3ba088bfa61e7c781c6e6cc95d4cdc9ce98c1ee070424c4a");
    }

    @Test
    @SneakyThrows
    public void shouldGenerateAHashFromAMultipleObjects() {
        String hash = hashGenerator.generateHash("Object 1", "Object 2");

        assertThat(hash).isEqualTo("7ca30a03c43d539842c53db6597a7ea583fa4f2b37a2a63bf67d087538282e27");
    }

    @Test
    @SneakyThrows
    public void shouldGenerateEqualHashesFromAMultipleObjectsWithNull() {
        String hash1 = hashGenerator.generateHash("Object 1", "Object 2");
        String hash2 = hashGenerator.generateHash("Object 1", null, "Object 2");

        assertThat(hash1).isEqualTo(hash2);
    }
}
