package com.github.teenhack42.cruisescanner;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TicketView extends AppCompatActivity {

	Hook h = null;

	Ticket ticket = null;
	String uid = null;

	TextView nameV;
	TextView crewV;
	TextView mobileV;

	ToggleButton admisionB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_view);

		uid = getIntent().getStringExtra("uid");

		ExecutorService es = Executors.newSingleThreadExecutor();

		Future result = es.submit(new downloadTicket(uid));
		try {
			ticket = (Ticket) result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		es.shutdown();


		nameV = findViewById(R.id.name);
		nameV.setText(ticket.rover.name());

		crewV = findViewById(R.id.crew);
		crewV.setText(ticket.rover.crew);

		mobileV = findViewById(R.id.mobile);
		mobileV.setText(ticket.rover.mobile);

		admisionB = findViewById(R.id.toggleAdmited);
		admisionB.setChecked(ticket.attendance);
		admisionB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Boolean attendance = ticket.attendance;

				if (admisionB.getText().toString().equals("On Board")) {
					// is on board....
					attendance = true;
				} else {
					attendance = false;
				}

				ExecutorService es = Executors.newSingleThreadExecutor();

				Future result = es.submit(new setAttendance(uid, attendance));
				try {
					ticket.attendance = (Boolean) result.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				es.shutdown();


				/*this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
				*/
			}
		});
	}
}

class downloadTicket implements Callable<Ticket> {

	String uid = null;

	downloadTicket(String uid) {
		this.uid = uid;
	}

	@Override
	public Ticket call() throws Exception {
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
			//paid = ticket.getBoolean("paid");
			//entered = ticket.getBoolean("entered");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return t;
	}
}

class setAttendance implements Callable<Boolean> {

	String uid = null;
	Boolean attendance = null;

	setAttendance(String uid, Boolean att) {
		this.uid = uid;
		this.attendance = att;
	}

	@Override
	public Boolean call() {
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

		JSONObject ticket;
		JSONObject rover;

		Log.d("TICKET JSON", ret);
		Boolean out = null;
		try {
			out = new JSONObject(ret).getBoolean("attendance");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return out;

	}
}
