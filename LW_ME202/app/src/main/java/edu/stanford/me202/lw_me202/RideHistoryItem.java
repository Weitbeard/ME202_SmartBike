package edu.stanford.me202.lw_me202;

import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Luke on 4/17/2017.
 */

public class RideHistoryItem extends RealmObject {

    @PrimaryKey
    @Required
    private String key;
    @Required
    private Date rideDate;
    @Required
    private String rideLocation;
    private int rideIconType;
    @Required
    private String FbKey;

     //need an empty constructor for realm
    public RideHistoryItem() {}

    public RideHistoryItem(String rideLocation, Date rideDate) {
         //store new data
        this.key = rideLocation + ": " + rideDate.toString();
        this.rideLocation = rideLocation;
        this.rideDate = rideDate;
         //assign a random icon type
        this.rideIconType = Math.abs( new Random().nextInt() );
         //assign an empty FbKey
        this.FbKey = "";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRideLocation() {
        return rideLocation;
    }

    public void setRideLocation(String rideLocation) {
        this.rideLocation = rideLocation;
    }

    public Date getRideDate() {
        return rideDate;
    }

    public void setRideDate(Date rideDate) {
        this.rideDate = rideDate;
    }

    public int getRideIconType() {
        return rideIconType;
    }

    public void setRideIconType(int rideIconType) {
        this.rideIconType = rideIconType;
    }

    public String getFbKey() {
        return FbKey;
    }

    public void setFbKey(String fbKey) {
        FbKey = fbKey;
    }
}
