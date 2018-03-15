package uk.co.mpcontracting.rpmdata.ng.model.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class Band extends AbstractData {
    private String image;
    private String biography;
    private String members;
    private String genres;
    @Getter private List<Album> albums;
    
    public Band(String name, String image, String biography, String members, String genres) {
        super(BAND_IDENTIFIER, name);
        
        this.image = trim(image);
        this.biography = trim(biography);
        this.members = trim(members);
        this.genres = trim(genres);
        
        albums = new ArrayList<Album>();
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public boolean isValid() {
        for (Album album : albums) {
            if (!album.getTracks().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    protected String getDataRowInternal() {
        return image + SEPARATOR + biography + SEPARATOR + members + SEPARATOR + genres;
    }
}