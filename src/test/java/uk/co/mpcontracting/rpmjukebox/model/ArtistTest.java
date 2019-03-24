package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateArtist;

@RunWith(MockitoJUnitRunner.class)
public class ArtistTest {

    @Test
    public void shouldTestHashCode() {
        Artist artist1 = generateArtist(1);
        Artist artist2 = Artist.builder()
                .artistId("1231")
                .build();

        assertThat(artist1.hashCode()).isEqualTo(artist2.hashCode());
    }

    @Test
    public void shouldTestEquals() {
        Artist artist1 = generateArtist(1);
        Artist artist2 = Artist.builder()
                .artistId("1231")
                .build();

        assertThat(artist1).isEqualTo(artist2);
    }
}
