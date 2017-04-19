package edu.stanford.me202.lw_me202;

import java.util.Random;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Luke on 4/17/2017.
 */

public class RideHistoryItem extends RealmObject {

    // TODO: 4/18/2017 replace the primary key with a unique hash-key, rather than using the ride location/label
    @PrimaryKey
    @Required
    private String rideLocation;
    @Required
    private String rideDate;
    private int rideIconType;

     //need an empty constructor
    public RideHistoryItem() {}

    public RideHistoryItem(String rideLocation, String rideDate) {
        this.rideLocation = rideLocation;
        this.rideDate = rideDate;
         //assign a random icon type
        this.rideIconType = Math.abs( new Random().nextInt() );
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

    public int getRideIconType() {
        return rideIconType;
    }

    public void setRideIconType(int rideIconType) {
        this.rideIconType = rideIconType;
    }
}
