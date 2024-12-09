package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettings {
  private String version;
  private int cacheSizeMb;
  private String proxyHost;
  private Integer proxyPort;
  private Boolean proxyRequiresAuthentication;
  private String proxyUsername;
  private String proxyPassword;
}
