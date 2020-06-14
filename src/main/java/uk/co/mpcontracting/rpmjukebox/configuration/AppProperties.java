package uk.co.mpcontracting.rpmjukebox.configuration;

import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties(prefix = "application")
public class AppProperties {

    @NotNull
    private String version;

    @URL
    @NotNull
    private String websiteUrl;

    @URL
    @NotNull
    private String versionUrl;

    @URL
    @NotNull
    private String dataFileUrl;

    @URL
    @NotNull
    private String s3BucketUrl;

    private int jettyPort;

    @NotNull
    private String logDirectory;

    @NotNull
    private String artistIndexDirectory;

    @NotNull
    private String trackIndexDirectory;

    @NotNull
    private String cacheDirectory;

    @NotNull
    private String lastIndexedFile;

    @NotNull
    private String windowSettingsFile;

    @NotNull
    private String systemSettingsFile;

    @NotNull
    private String userSettingsFile;

    private double defaultVolume;
    private int maxSearchHits;
    private int maxPlaylistSize;
    private int shuffledPlaylistSize;
    private int previousSecondsCutoff;

    @NotNull
    private String playlistFileExtension;

    private int cacheSizeMb;
}
