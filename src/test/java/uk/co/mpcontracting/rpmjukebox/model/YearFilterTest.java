package uk.co.mpcontracting.rpmjukebox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createYearString;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearFilterTest {

  @Test
  void shouldReturnDisplayFromToString() {
    String display = getFaker().lorem().characters(20, 30);
    YearFilter yearFilter = new YearFilter(display, createYearString());

    assertThat(yearFilter.toString()).isEqualTo(display);
  }
}