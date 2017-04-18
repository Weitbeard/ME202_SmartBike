package edu.stanford.me202.lw_me202;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Luke on 4/17/2017.
 */

public class RideHistoryItem extends RealmObject {

    // TODO: 4/18/2017 replace the primary key with a unique hash-key, rather than using the location
    @PrimaryKey
    @Required
    private String rideLocation;

    @Required
    private String rideDate;

     //need an empty constructor
    public RideHistoryItem() {}

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
