package uk.co.mpcontracting.rpmjukebox.model;

public record YearFilter(String display, String year) {

  @Override
  public String toString() {
    return display;
  }
}
