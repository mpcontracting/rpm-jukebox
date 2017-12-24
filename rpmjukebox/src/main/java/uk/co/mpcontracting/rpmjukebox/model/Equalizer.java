package uk.co.mpcontracting.rpmjukebox.model;

import lombok.Getter;

public class Equalizer {
    @Getter
    private double[] gain;

    public Equalizer(int numberOfBands) {
        gain = new double[numberOfBands];
    }

    public int getNumberOfBands() {
        return gain.length;
    }

    public void setGain(int band, double value) {
        if (band < 0 || band > (gain.length - 1)) {
            throw new IllegalArgumentException("Only " + gain.length + " EQ bands are available - " + band);
        }

        gain[band] = value;
    }

    public double getGain(int band) {
        if (band < 0 || band > (gain.length - 1)) {
            throw new IllegalArgumentException("Only " + gain.length + " EQ bands are available - " + band);
        }

        return gain[band];
    }
}
