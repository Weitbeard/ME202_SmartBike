package edu.stanford.me202.lw_me202;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";

    private RecyclerView rideHistoryRcycView;
    private RecyclerView.Adapter rideHistoryAdapter;
    private RecyclerView.LayoutManager rideHistoryLayoutManager;
    private ArrayList<RideHistoryItem> rideData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.activity_ride_history);
        rideHistoryRcycView = (RecyclerView) findViewById(R.id.rideHistoryRcyc_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rideHistoryRcycView.setHasFixedSize(true);

        // use a linear layout manager
        rideHistoryLayoutManager = new LinearLayoutManager(this);
        rideHistoryRcycView.setLayoutManager(rideHistoryLayoutManager);

        // specify an adapter (see also next example)
        rideHistoryAdapter = new RideHistoryRcycAdapter(rideData);
        rideHistoryRcycView.setAdapter(rideHistoryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: 4/17/2017  
    }

    private void initData(){
        rideData.add(new RideHistoryItem("Tulsa, OK","01/04/1991"));
        rideData.add(new RideHistoryItem("Omaha, NE","05/21/2009"));
        rideData.add(new RideHistoryItem("Stanford, CA","06/23/2017"));
    }
}
