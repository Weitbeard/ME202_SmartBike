package edu.stanford.me202.lw_me202;

import android.app.Application;

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
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);

//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        realm.deleteAll();
//        realm.commitTransaction();
    }
}
