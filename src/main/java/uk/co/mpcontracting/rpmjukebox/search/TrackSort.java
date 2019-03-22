package uk.co.mpcontracting.rpmjukebox.search;

public enum TrackSort {
    DEFAULTSORT("Default"), ARTISTSORT("Artist"), ALBUMSORT("Album"), TRACKSORT("Track");

    private String friendlyName;

    TrackSort(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
