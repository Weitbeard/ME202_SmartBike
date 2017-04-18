package edu.stanford.me202.lw_me202;

/**
 * Created by Luke on 4/17/2017.
 */

public class RideHistoryItem {

    private String rideLocation;
    private String rideDate;

    public RideHistoryItem(String rideLocation, String rideDate) {
        this.rideLocation = rideLocation;
        this.rideDate = rideDate;
    }

    public String getRideLocation() {
        return rideLocation;
    }

    public void setRideLocation(String rideLocation) {
        this.rideLocation = rideLocation;
    }

    public String getRideDate() {
        return rideDate;
    }

    public void setRideDate(String rideDate) {
        this.rideDate = rideDate;
    }
}
