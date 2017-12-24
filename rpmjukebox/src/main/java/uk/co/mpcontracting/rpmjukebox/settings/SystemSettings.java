package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString(includeFieldNames = true)
public class SystemSettings {
    @Getter
    @Setter
    private int cacheSizeMb;
}
