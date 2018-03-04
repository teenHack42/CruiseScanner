package com.github.teenhack42.cruisescanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by grant on 11/2/18.
 */

public class TicketAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<Ticket> mDataSource;

	public TicketAdapter(Context context, ArrayList<Ticket> items) {
		mContext = context;
		mDataSource = items;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int i) {
		return mDataSource.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get view for row item
		View rowView = mInflater.inflate(R.layout.ticket_item, parent, false);

		TextView name = (TextView) rowView.findViewById(R.id.name);
		TextView crew = (TextView) rowView.findViewById(R.id.crew);
		TextView mobile = (TextView) rowView.findViewById(R.id.mobile);

		ImageView attendanceColour = (ImageView) rowView.findViewById(R.id.attendanceBadge);

		Ticket ticket = (Ticket) getItem(position);
		final String ticketUID = ticket.uid;

		name.setText(ticket.rover.fname + " " + ticket.rover.lname);
		crew.setText(ticket.rover.crew);
		mobile.setText(ticket.rover.mobile);
		attendanceColour.setBackgroundColor(ticket.attendance ? Color.GREEN : Color.RED);

		rowView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent myIntent = new Intent(mContext.getApplicationContext(), TicketView.class);
				myIntent.putExtra("uid", ticketUID);
				mContext.startActivity(myIntent);
			}
		});

		return rowView;
	}


}
