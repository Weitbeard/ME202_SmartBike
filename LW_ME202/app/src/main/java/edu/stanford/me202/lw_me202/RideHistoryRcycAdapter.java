package edu.stanford.me202.lw_me202;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Luke on 4/15/2017.
 */

public class RideHistoryRcycAdapter extends RecyclerView.Adapter<RideHistoryRcycAdapter.ViewHolder> {

    // Provide a suitable constructor (depends on the kind of dataset)
    public RideHistoryRcycAdapter() {}

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rideItemIcon_image) ImageView rideIcon;
        @BindView(R.id.rideItemLocation_text) TextView rideLocationText;
        @BindView(R.id.rideItemDate_text) TextView rideDateText;

        public ViewHolder(View v) {
            super(v);

             //bind views
            ButterKnife.bind(this, v);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RideHistoryRcycAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_ride_history, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        RideHistoryItem rh;

        try(Realm realm = Realm.getDefaultInstance()){
            RealmResults<RideHistoryItem> rr = realm.where(RideHistoryItem.class).findAll();
            rh = rr.get(position);
        }

        //holder.rideIcon....; TODO: add icons to each row
        holder.rideLocationText.setText(rh.getRideLocation());
        holder.rideDateText.setText(rh.getRideDate());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        RealmResults<RideHistoryItem> rr;
        try(Realm realm = Realm.getDefaultInstance()){
            rr = realm.where(RideHistoryItem.class).findAll();
        }
        return rr.size();
    }

}
