package uk.co.mpcontracting.rpmjukebox.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class YearFilterTest extends AbstractTest {

    @Test
    public void shouldReturnDisplayFromToString() {
        YearFilter yearFilter = new YearFilter("Display", "2000");
        
        assertThat("Year filter toString() should be 'Display'", yearFilter.toString(), equalTo("Display"));
    }
}
