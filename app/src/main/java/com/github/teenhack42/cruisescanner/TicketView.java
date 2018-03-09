package com.github.teenhack42.cruisescanner;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.teenhack42.CruiseScanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;

public class TicketView extends Activity {

	String uid = null;

	ToggleButton admisionB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_view);

		uid = getIntent().getStringExtra("uid");
		Log.d("UID", uid);

		new dlTicket(CruiseScanner.getAppContext(), findViewById(android.R.id.content)).execute(uid);

		admisionB = findViewById(R.id.toggleAdmited);
		admisionB.setVisibility(View.INVISIBLE);

		admisionB.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
				new setAttendance().execute(new Attendance(uid, isChecked));
			}
		}) ;

	}
}

class dlTicket extends AsyncTask<String, Integer, Ticket> {
	private Context mContext;
	private View rootView;

	public dlTicket(Context context, View rootView) {
		this.mContext=context;
		this.rootView=rootView;
	}

	@Override
	protected Ticket doInBackground(String... strings) {
		String uid = strings[0];

		Hook h = null;
		Ticket t = null;
		if (h == null) {
			try {
				h = new Hook("http://192.168.0.12:3000/app/hook");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("uid", uid);

		String ret = h.post("get_ticket", params);

		JSONObject ticket;
		JSONObject rover;

		try {

			Log.d("TICKET JSON", ret);

			ticket = new JSONObject(ret);
			rover = ticket.getJSONObject("rover");

			t = new Ticket(ticket.getString("uid"), new Rover(rover.getString("uid"), rover.getString("fname"), rover.getString("lname")));
			t.attendance = ticket.getBoolean("attendance");
			t.rover.crew = rover.getString("crew");
			t.rover.mobile = rover.getString("mobile");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return t;
	}


	@Override
	protected void onPostExecute(Ticket result) {
		Log.d("dlTicket", "Downloaded");
		TextView name = rootView.findViewById(R.id.name);
		name.setText(result.rover.fname + " " + result.rover.lname);

		TextView crew = rootView.findViewById(R.id.crew);
		crew.setText(result.rover.crew);

		TextView mobile = rootView.findViewById(R.id.mobile);
		mobile.setText(result.rover.mobile);

		ToggleButton toggle = rootView.findViewById(R.id.toggleAdmited);
		toggle.setChecked(result.attendance);
		toggle.setVisibility(View.VISIBLE);
	}
}

class Attendance {
	String muid;
	Boolean matt;

	public Attendance(String uid, Boolean att) {
		this.muid = uid;
		this.matt = att;
	}
}

class setAttendance extends AsyncTask<Attendance, Integer, Attendance> {

	setAttendance() {}

	@Override
	protected Attendance doInBackground(Attendance... strings) {
		String uid = strings[0].muid;
		Boolean attendance = strings[0].matt;

		Hook h = null;
		Ticket t = null;
		if (h == null) {
			try {
				h = new Hook("http://192.168.0.12:3000/app/hook");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("uid", uid);
		params.put("attendance", attendance);

		String ret = h.post("set_attendance", params);


		Attendance retAtt = null;
		try {
			 retAtt = new Attendance(new JSONObject(ret).getString("uid"), new JSONObject(ret).getBoolean("attendance"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return retAtt;
	}
}
