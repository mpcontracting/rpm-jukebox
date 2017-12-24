package uk.co.mpcontracting.rpmjukebox.search;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackSortTest extends AbstractTest {

    @Test
    public void shouldReturnFriendlyName() {
        String friendlyName = TrackSort.DEFAULTSORT.getFriendlyName();

        assertThat("Friendly name should be 'Default'", friendlyName, equalTo("Default"));
    }
}
