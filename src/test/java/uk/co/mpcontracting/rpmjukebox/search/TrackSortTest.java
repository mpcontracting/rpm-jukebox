package uk.co.mpcontracting.rpmjukebox.search;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackSortTest {

    @Test
    public void shouldReturnFriendlyName() {
        String friendlyName = TrackSort.DEFAULTSORT.getFriendlyName();

        assertThat(friendlyName).isEqualTo("Default");
    }
}
