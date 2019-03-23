package uk.co.mpcontracting.rpmjukebox.model;

import lombok.Builder;
import lombok.Value;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Value
@Builder
public class Artist {
    private final String artistId;
    private final String artistName;
    private final String artistImage;
    private final String biography;
    private final String members;

    @Override
    public int hashCode() {
        return reflectionHashCode(this,
                artistName,
                artistImage,
                biography,
                members);
    }

    @Override
    public boolean equals(Object object) {
        return reflectionEquals(this, object,
                artistName,
                artistImage,
                biography,
                members);
    }
}
