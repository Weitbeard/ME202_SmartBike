package edu.stanford.me202.lw_me202;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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

    Realm realm;

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
                removeFromRideHistory(viewHolder.getAdapterPosition());
                 //show toast
                Toast toast = Toast.makeText(getApplicationContext(),R.string.itemDeletedToast_text, Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryRcyc);

         //initialize realm for the activity
        realm = Realm.getDefaultInstance();
        //initTestData();

         //pull in picture
        Picasso.with(this)
                .load("https://media.licdn.com/mpr/mpr/shrinknp_400_400/AAEAAQAAAAAAAALeAAAAJDU1NzMzM2JmLTgxZDMtNGEwYy1hODk1LTY5ZWNiMmEzZjY3MA.jpg")
                .transform(new CircleConvert())
                .into(rideHistoryBanner);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        rideHistoryUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newRide = rideHistoryEntry.getText().toString();
                if(!newRide.equals("")){
                     //minimize keyboard
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(rideHistoryEntry.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                     //get the current date
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    String formattedDate = dateFormat.format(c.getTime());
                     //add new item to the list
                    addToRideHistory(rideHistoryEntry.getText().toString(),formattedDate);
                     //clear the text entry and remove focus
                    rideHistoryEntry.setText("");
                    rideHistoryEntry.clearFocus();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void addToRideHistory(String rideLocation, String rideDate){
        realm.beginTransaction();
         //check to confirm it doesn't already exist
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).equalTo("rideLocation",rideLocation).findAll();
        if(rides.isEmpty()){
            //create object
            RideHistoryItem r = new RideHistoryItem(rideLocation, rideDate);
            //add it to realm
            realm.copyToRealm(r);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),R.string.badItemEntryToast_text, Toast.LENGTH_SHORT);
            toast.show();
        }
        realm.commitTransaction();
         //add it to the list
        rideHistoryRcyc.getAdapter().notifyDataSetChanged();
    }

    private void removeFromRideHistory(int position){
         //remove it from realm
        realm.beginTransaction();
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).findAll();
        rides.get(position).deleteFromRealm();
        realm.commitTransaction();
         //remove object from the list
        rideHistoryRcyc.getAdapter().notifyDataSetChanged();
    }

//    private void initTestData(){
//         //if there is nothing in realm
//        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).findAll();
//        if(rides.isEmpty()){
//             //add some dummy initial data
//            addToRideHistory("Tulsa, OK", "01/04/1991");
//            addToRideHistory("Omaha, NE", "05/21/2009");
//            addToRideHistory("Stanford, CA", "06/23/2017");
//        }
//    }

}
