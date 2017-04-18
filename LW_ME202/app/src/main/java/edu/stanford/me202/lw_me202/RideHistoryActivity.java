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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";

    private RecyclerView rideHistoryRcycView;
    private RecyclerView.Adapter rideHistoryAdapter;
    private RecyclerView.LayoutManager rideHistoryLayoutManager;
    //private ArrayList<RideHistoryItem> rideData = new ArrayList<>();

    private ImageView rideHistoryBanner;
    private EditText rideHistoryEntry;
    private Button rideHistoryUpdateButton;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ride_history);
        rideHistoryRcycView = (RecyclerView) findViewById(R.id.rideHistoryRcyc);
        //rideHistoryRcycView.setHasFixedSize(true);
        rideHistoryLayoutManager = new LinearLayoutManager(this);
        rideHistoryRcycView.setLayoutManager(rideHistoryLayoutManager);
        rideHistoryAdapter = new RideHistoryRcycAdapter();
        rideHistoryRcycView.setAdapter(rideHistoryAdapter);

         //bind other views
        rideHistoryBanner = (ImageView) findViewById(R.id.rideHistoryBanner);
        rideHistoryEntry = (EditText) findViewById(R.id.rideHistoryEntry);
        rideHistoryUpdateButton = (Button) findViewById(R.id.rideHistoryUpdateButton);

         //setup swipe-to-delete
        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                // TODO: add a soft-delete function & confirmation dialog (or snackbar?)
                /* //open up a dialog to confirm removal
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RideHistoryActivity.this);
                dialogBuilder.setTitle("Removal Confirmation")
                        .setMessage("Are you sure you want to delete this ride?")
                        .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                 */

                 //remove the swiped ride from the list
                removeFromRideHistory(viewHolder.getAdapterPosition());
                 //show toast
                Toast toast = Toast.makeText(getApplicationContext(),R.string.itemDeletedToast_text, Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryRcycView);

         //initialize realm for the activity
        realm = Realm.getDefaultInstance();
        initTestData();
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
                     //minimize keyboard (...needs review...)
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                else{
                     //do nothing
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
        rideHistoryRcycView.getAdapter().notifyDataSetChanged();
    }

    private void removeFromRideHistory(int position){
         //remove it from realm
        realm.beginTransaction();
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).findAll();
        rides.get(position).deleteFromRealm();
        realm.commitTransaction();
         //remove object from the list
        rideHistoryRcycView.getAdapter().notifyDataSetChanged();
    }

    private void initTestData(){
         //if there is nothing in realm
        RealmResults<RideHistoryItem> rides = realm.where(RideHistoryItem.class).findAll();
        if(rides.isEmpty()){
             //add some dummy initial data
            addToRideHistory("Tulsa, OK", "01/04/1991");
            addToRideHistory("Omaha, NE", "05/21/2009");
            addToRideHistory("Stanford, CA", "06/23/2017");
        }
    }

}
