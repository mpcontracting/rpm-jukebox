package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SystemSettings {
    private String version;
    private int cacheSizeMb;
    private String proxyHost;
    private Integer proxyPort;
    private Boolean proxyRequiresAuthentication;
    private String proxyUsername;
    private String proxyPassword;
}
