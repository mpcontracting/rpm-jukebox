package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class YearFilterTest {

    @Test
    public void shouldReturnDisplayFromToString() {
        YearFilter yearFilter = new YearFilter("Display", "2000");

        assertThat(yearFilter.toString()).isEqualTo("Display");
    }
}
