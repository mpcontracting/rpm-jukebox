package uk.co.mpcontracting.rpmjukebox.search;

public enum TrackSort {
    DEFAULT_SORT("Default"), ARTIST_SORT("Artist"), ALBUM_SORT("Album"), TRACK_SORT("Track");

    private final String friendlyName;

    TrackSort(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
