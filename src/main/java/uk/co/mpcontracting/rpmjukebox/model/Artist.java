package uk.co.mpcontracting.rpmjukebox.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Artist {
  @EqualsAndHashCode.Include String artistId;
  String artistName;
  String artistImage;
  String biography;
  String members;
}
