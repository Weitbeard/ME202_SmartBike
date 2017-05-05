package edu.stanford.me202.lw_me202;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";

    @BindView(R.id.rideHistoryRcyc) RecyclerView rideHistoryRcyc;
    @BindView(R.id.rideHistoryBanner) ImageView rideHistoryBanner;
    @BindView(R.id.rideHistoryEntry) EditText rideHistoryEntry;
    @BindView(R.id.rideHistoryUpdateButton) Button rideHistoryUpdateButton;

    private Realm realm;
    private FirebaseDatabase bikeDB;
    private DatabaseReference rideRef;


    private SharedPreferences mPrefs;
    static final String SYNC_STATE = "SYNC_STATE";
    private boolean synced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //link the proper layout
        setContentView(R.layout.activity_ride_history);
         //bind views
        ButterKnife.bind(this);
         //initialize recycler view utilities
        RecyclerView.LayoutManager rideHistoryLayoutManager = new LinearLayoutManager(this);
        rideHistoryRcyc.setLayoutManager(rideHistoryLayoutManager);
        RecyclerView.Adapter rideHistoryAdapter = new RideHistoryRcycAdapter(this);
        rideHistoryRcyc.setAdapter(rideHistoryAdapter);

         //setup swipe-to-delete
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // TODO: add a soft-delete function & confirmation dialog (or snackbar?)
                 //remove the swiped ride from the list
                removeFromRides(viewHolder.getAdapterPosition());
                 //show toast
                Toast toast = Toast.makeText(getApplicationContext(),R.string.itemDeletedToast_text, Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryRcyc);

        //pull in default user picture
        Picasso.with(this)
                //grab the profile picture off linkedin
                .load(getString(R.string.userPicture_URL))
                .transform(new CircleConvert())
                .into(rideHistoryBanner);

         //initialize realm for the activity
        realm = Realm.getDefaultInstance();
         //get reference to Firebase database;
        bikeDB = FirebaseDatabase.getInstance();
        rideRef = bikeDB.getReference();

        if(bikeDB != null) {
            //do initial sync of data from Firebase
            if (!synced) {
                syncWithFirebase();
                synced = true;
            }

            //setup the database reference listeners
            // TODO: 5/3/2017 setup listeners to respond to changes from database side (not needed for the lab)
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        //save sync status to saved state
        savedInstanceState.putBoolean(SYNC_STATE,this.synced);
        Log.d(TAG,"Saved instance state; synced: "+this.synced);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        //restore sync status from saved state
        this.synced = savedInstanceState.getBoolean(SYNC_STATE);
        Log.d(TAG,"Restored instance state; synced: "+this.synced);
    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onResume() {
        super.onResume();

        rideHistoryUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //get ride label for entry to be added
                String newRide = rideHistoryEntry.getText().toString();
                 //if the new ride item is not blank
                if(!newRide.equals("")){
                     //minimize keyboard
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(rideHistoryEntry.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                     //get the current date
                    Calendar c = Calendar.getInstance();
                     //create object
                    RideHistoryItem r = new RideHistoryItem(rideHistoryEntry.getText().toString(), c.getTime());
                     //add new item to the list
                    addToRides(r);
                     //clear the text entry and remove focus
                    rideHistoryEntry.setText("");
                    rideHistoryEntry.clearFocus();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
         //dumb work around for to maintain a persistence for the sync state...
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         //close realm instance
        realm.close();
    }

    private void addToRides(RideHistoryItem r){
        //add new item to Firebase
        if (bikeDB != null) {
            writeToFirebase(r); //also sets the FbKey for ride
            Log.d(TAG,"ride written to firebase with key: "+r.getFbKey());
        }
        //add new item to Realm
        writeToRealm(r);
    }

    private void removeFromRides(int position){
         //open a new transaction with the realm db
        realm.beginTransaction();
         //get the ride to be deleted
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).findAll();
         //if there is a Firebase connection...
        if((rideRef != null) && (!rides.get(position).getFbKey().isEmpty())) {
            Log.d(TAG,"deleting ride: "+ rides.get(position).getFbKey());
            //remove the ride from Firebase
            removeFromFirebase(rides.get(position));
        }
         //remove the ride from realm
        rides.get(position).deleteFromRealm();
         //close the transaction with the realm db
        realm.commitTransaction();
         //remove object from the list
        rideHistoryRcyc.getAdapter().notifyDataSetChanged();
    }

    private void writeToRealm(RideHistoryItem ride){
        //open a new transaction with the realm db
        realm.beginTransaction();
        //check to confirm it doesn't already exist
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).equalTo("rideDate",ride.getRideDate()).findAll();
        if(rides.isEmpty()){
            //add it to realm
            realm.copyToRealm(ride);
        }
        else{
//            Toast toast = Toast.makeText(getApplicationContext(),R.string.badItemEntryToast_text, Toast.LENGTH_SHORT);
//            toast.show();
        }
        //close the transaction with the realm db
        realm.commitTransaction();
        //add it to the displayed list
        rideHistoryRcyc.getAdapter().notifyDataSetChanged();
    }

    private void writeToFirebase(RideHistoryItem ride){
         //get a key from Firebase for the new ride
        String newKey = rideRef.child("rides").push().getKey();
         //store the key locally
        ride.setFbKey(newKey);
         //add the new ride to firebase using the key
        rideRef.child("rides").child(newKey).setValue(ride).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //do nothing
                        }
                        else{
                            //display error toast
                            Toast toast = Toast.makeText(getApplicationContext(),"Firebase write error", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
        );
    }

    private void removeFromFirebase(RideHistoryItem ride){
        //Query queryRef = rideRef.child("rides").equalTo(ride.getFbKey()).limitToFirst(1);
        Log.d(TAG,"trying to delete: "+rideRef.child("rides").child(ride.getFbKey()).toString());
        rideRef.child("rides").child(ride.getFbKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast toast = Toast.makeText(getApplicationContext(),"Successful Firebase Deletion", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Firebase delete error", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private void syncWithFirebase(){
        Log.d(TAG,"Trying to sync");
        DatabaseReference syncRef =  bikeDB.getReference().child("rides");
        syncRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot rideSnapshot: dataSnapshot.getChildren()) {
                    RideHistoryItem ride = rideSnapshot.getValue(RideHistoryItem.class);
                    Log.d(TAG, "Adding item to realm: "+ride.toString());
                    writeToRealm(ride);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
