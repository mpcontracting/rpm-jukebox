package uk.co.mpcontracting.rpmdata.ng.model.json;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class JsonTrack {
    private Integer id;
    private String title;
    private String artist;
    @SerializedName("id3_genre")
    private String genre;
    private String file;
    private String cover;
    @SerializedName("created_by")
    private Integer createdBy;
}
