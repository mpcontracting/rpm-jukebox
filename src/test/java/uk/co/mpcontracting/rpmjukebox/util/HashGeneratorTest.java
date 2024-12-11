package uk.co.mpcontracting.rpmjukebox.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HashGeneratorTest {

  private HashGenerator underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new HashGenerator();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWithNullHashKeys() {
    assertThatThrownBy(() -> underTest.generateHash((Object[]) null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWithNoHashKeys() {
    assertThatThrownBy(() -> underTest.generateHash())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionIfKeyLengthIsZero() {
    assertThatThrownBy(() -> underTest.generateHash("", "")).isInstanceOf(RuntimeException.class);
  }

  @Test
  @SneakyThrows
  void shouldGenerateAHashFromASingleObject() {
    String hash = underTest.generateHash("Object 1");

    assertThat(hash).isEqualTo("9c98295d0c3d33bf3ba088bfa61e7c781c6e6cc95d4cdc9ce98c1ee070424c4a");
  }

  @Test
  @SneakyThrows
  void shouldGenerateAHashFromAMultipleObjects() {
    String hash = underTest.generateHash("Object 1", "Object 2");

    assertThat(hash).isEqualTo("7ca30a03c43d539842c53db6597a7ea583fa4f2b37a2a63bf67d087538282e27");
  }

  @Test
  @SneakyThrows
  void shouldGenerateEqualHashesFromAMultipleObjectsWithNull() {
    String hash1 = underTest.generateHash("Object 1", "Object 2");
    String hash2 = underTest.generateHash("Object 1", null, "Object 2");

    assertThat(hash1).isEqualTo(hash2);
  }
}