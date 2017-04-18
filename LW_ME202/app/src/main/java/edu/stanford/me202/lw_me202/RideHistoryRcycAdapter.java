package edu.stanford.me202.lw_me202;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Luke on 4/15/2017.
 */

public class RideHistoryRcycAdapter extends RecyclerView.Adapter<RideHistoryRcycAdapter.ViewHolder> {

    private ArrayList<RideHistoryItem> dataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public RideHistoryRcycAdapter(ArrayList<RideHistoryItem> rideData) { this.dataset = rideData; }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView rideIcon;
        public TextView rideLocationText;
        public TextView rideDateText;
        public ViewHolder(View v) {
            super(v);
            rideIcon = (ImageView) v.findViewById(R.id.rideItemIcon_image);
            rideLocationText = (TextView) v.findViewById(R.id.rideItemLocation_text);
            rideDateText = (TextView) v.findViewById(R.id.rideItemDate_text);
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
        //holder.rideIcon....; todo
        holder.rideLocationText.setText(dataset.get(position).getRideLocation());
        holder.rideDateText.setText(dataset.get(position).getRideDate());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

}
