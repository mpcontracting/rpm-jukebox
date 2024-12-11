package uk.co.mpcontracting.rpmjukebox.search;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.search.TrackSort.DEFAULT_SORT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackSortTest {

  @Test
  void shouldReturnFriendlyName() {
    String friendlyName = DEFAULT_SORT.getFriendlyName();

    assertThat(friendlyName).isEqualTo("Default");
  }
}