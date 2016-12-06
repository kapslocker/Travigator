package com.frodo.travigator.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frodo.travigator.R;
import com.frodo.travigator.models.Stop;

/**
 * Created by durgesh on 4/29/16.
 */
public class StopListAdapter extends BaseAdapter {
    private Stop[] stops;
    private LayoutInflater layoutInflater;
    private int[] status;
    private Context context;
    public static final int STATUS_INACTIVE = 0, STATUS_VISITED=1, STATUS_REMANING=2, STATUS_CURRENT=3;
    public StopListAdapter(Context context, Stop[]stops, int[] status) {
        this.stops = stops;
        this.status = status;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void changeStatus(int[] status) {
        this.status = status;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.stops.length;
    }

    @Override
    public Object getItem(int position) {
        return this.stops[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = layoutInflater.inflate(R.layout.stops_list_item, null) ;
        TextView name = (TextView)rootView.findViewById(R.id.stop_name);
        View statusView = rootView.findViewById(R.id.status);
        if (status[position] == STATUS_INACTIVE || status[position] == STATUS_VISITED) {
            statusView.setBackground(ContextCompat.getDrawable(context,R.drawable.circle_red));
        } else if(status[position] == STATUS_CURRENT) {
            statusView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
        } else {
            statusView.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_yello));
        }
        name.setText(stops[position].getStop_name());
        return rootView;
    }
}
