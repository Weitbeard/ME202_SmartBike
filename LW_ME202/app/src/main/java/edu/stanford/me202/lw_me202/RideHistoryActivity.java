package edu.stanford.me202.lw_me202;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";

    private RecyclerView rideHistoryRcycView;
    private RecyclerView.Adapter rideHistoryAdapter;
    private RecyclerView.LayoutManager rideHistoryLayoutManager;
    private ArrayList<RideHistoryItem> rideData = new ArrayList<>();

    private ImageView rideHistoryBanner;
    private EditText rideHistoryEntry;
    private Button rideHistoryUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         //initializations for the data and recycler view
        initData();

        setContentView(R.layout.activity_ride_history);
        rideHistoryRcycView = (RecyclerView) findViewById(R.id.rideHistoryRcyc);
        //rideHistoryRcycView.setHasFixedSize(true);
        rideHistoryLayoutManager = new LinearLayoutManager(this);
        rideHistoryRcycView.setLayoutManager(rideHistoryLayoutManager);
        rideHistoryAdapter = new RideHistoryRcycAdapter(rideData);
        rideHistoryRcycView.setAdapter(rideHistoryAdapter);

         //bind other views
        rideHistoryBanner = (ImageView) findViewById(R.id.rideHistoryBanner);
        rideHistoryEntry = (EditText) findViewById(R.id.rideHistoryEntry);
        rideHistoryUpdateButton = (Button) findViewById(R.id.rideHistoryUpdateButton);

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
                rideData.remove(viewHolder.getAdapterPosition());
                rideHistoryRcycView.getAdapter().notifyDataSetChanged();
                 //show toast
                Toast toast = Toast.makeText(getApplicationContext(),R.string.itemDeletedToast_text, Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rideHistoryRcycView);
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
                    rideData.add(new RideHistoryItem(rideHistoryEntry.getText().toString(),formattedDate));
                    rideHistoryRcycView.getAdapter().notifyDataSetChanged();
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

    private void initData(){
        rideData.add(new RideHistoryItem("Tulsa, OK","01/04/1991"));
        rideData.add(new RideHistoryItem("Omaha, NE","05/21/2009"));
        rideData.add(new RideHistoryItem("Stanford, CA","06/23/2017"));
    }
}
