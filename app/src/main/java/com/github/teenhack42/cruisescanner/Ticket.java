package com.github.teenhack42.cruisescanner;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by grant on 11/2/18.
 */

public class Ticket {

	public String uid = null;
	public Rover rover = null;
	public Boolean attendance = null;

	public Ticket(String uid, Rover r) {
		this.uid = uid;
		this.rover = r;
	}

	public static ArrayList<Ticket> getTicketsFromServer() {
		ArrayList<Ticket> list = null;
		ExecutorService es = Executors.newSingleThreadExecutor();

		Future result = es.submit(new TicketDownloader());
		try {
			list = (ArrayList<Ticket>) result.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		es.shutdown();

		return list;
	}

	public static ArrayList<Ticket> searchTicketsFromServer(String query) {
		ArrayList<Ticket> list = null;
		ExecutorService es = Executors.newSingleThreadExecutor();

		Future result = es.submit(new TicketDownloader(query));
		try {
			list = (ArrayList<Ticket>) result.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		es.shutdown();

		return list;
	}
}

class Rover {
	public String uid = null;
	public String fname = null;
	public String lname = null;
	public String crew = null;
	public String mobile = null;


	public Rover(String uid, String fname, String lname) {
		this.uid = uid;
		this.fname = fname;
		this.lname = lname;
	}

	public Rover(String uid, String fname, String lname, String crew) {
		this.uid = uid;
		this.fname = fname;
		this.lname = lname;
		this.crew = crew;
	}

	public String name() {
		return this.fname + " " + this.lname;
	}
}

class TicketDownloader implements Callable {

	private String mQuery ;

	public TicketDownloader(String q) {
		this.mQuery = q;
	}

	public TicketDownloader() {
		this.mQuery = null;
	}

	@Override
	public List<Ticket> call() throws Exception {
		return downloadTickets();
	}

	private List<Ticket> downloadTickets() {
		Hook h = null;

		List<Ticket> list = new ArrayList<>();

		try {
			h = new Hook();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		HashMap<String, Object> params = new HashMap<>();

		if(this.mQuery != null) {
			params.put("query", this.mQuery);
		}

		if (!(h == null)) {
			String data = h.post("get_ticket_list", params);
			Log.d("DownloadTickets", data);
			JSONArray jsonA = null;
			try {
				jsonA = new JSONArray(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			for (int on = 0; on < jsonA.length(); on++) {
				JSONObject t = null;
				JSONObject r = null;
				try {
					t = jsonA.getJSONObject(on);
					r = t.getJSONObject("rover");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				try {
					Ticket temp = new Ticket(t.getString("uid"), new Rover(r.getString("uid"), r.getString("fname"), r.getString("lname"), r.getString("crew")));
					temp.rover.mobile = r.getString("mobile");
					temp.rover.crew = r.getString("crew");
					temp.attendance = t.getBoolean("attendance");
					list.add(temp);
				} catch (JSONException e) {
					e.printStackTrace();
				}


			}
		} else {
			return list;
		}

		return list;
	}
}