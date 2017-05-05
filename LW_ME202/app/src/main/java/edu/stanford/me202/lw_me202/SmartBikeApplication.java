package edu.stanford.me202.lw_me202;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;

import io.realm.Realm;
import io.realm.RealmConfiguration;

//import io.realm.Realm;
//import io.realm.RealmConfiguration;

/**
 * Created by Luke on 4/17/2017.
 */

public class SmartBikeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize Realm db within the application
        Realm.init(this);
        //configure the Realm db
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);

         //Killing all realm data up-front to test Firebase sync
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }
}
