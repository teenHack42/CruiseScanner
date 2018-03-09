package com.github.teenhack42.cruisescanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.teenhack42.CruiseScanner;

import java.util.ArrayList;

public class TicketSearch extends Activity {

	Button searchB;
	EditText searchF;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_search);

		new loadList(CruiseScanner.getAppContext(), findViewById(android.R.id.content)).execute();

		searchB = findViewById(R.id.searchButton);
		searchF = findViewById(R.id.searchField);

		searchB.setOnClickListener(new searchTickets(searchF));
	}

	@Override
	protected void onResume() {
		super.onResume();

		//reload when we come back every time to this list eg after editing details
		new loadList(CruiseScanner.getAppContext(), findViewById(android.R.id.content)).execute();
// 1


	}
}

class searchTickets implements View.OnClickListener {

	private EditText searchF;
	private String mQuery;

	public searchTickets(EditText sf) {
		this.searchF = sf;
	}

	@Override
	public void onClick(View view) {
		this.mQuery = searchF.getText().toString();
		new loadList(CruiseScanner.getAppContext(), view.getRootView(), this.mQuery).execute();
	}
}

class loadList extends AsyncTask<Void, Integer, ArrayList<Ticket>> {

	private Context mContext;
	private View rootView;
	private String mQuery;

	public loadList(Context context, View rootView) {
		this.mContext = context;
		this.rootView = rootView;
		this.mQuery = null;
	}

	public loadList(Context context, View rootView, String query) {
		this.mContext = context;
		this.rootView = rootView;
		if (query.length() > 0) {
			this.mQuery = query;
		} else {
			this.mQuery = null;
		}
	}

	@Override
	protected ArrayList<Ticket> doInBackground(Void... voids) {
		final ArrayList<Ticket> ticketList;
		if (this.mQuery == null) {
			ticketList = Ticket.getTicketsFromServer();
		} else {
			ticketList = Ticket.searchTicketsFromServer(this.mQuery);
		}
		return ticketList;
	}

	@Override
	protected void onPostExecute(ArrayList<Ticket> tickets) {
		ListView mListView = rootView.findViewById(R.id.searchResults);
		TicketAdapter adapter = new TicketAdapter(this.mContext, tickets);

		if (mListView != null) {
			if (tickets != null) {
				mListView.setAdapter(adapter);
			}
		} else {
			Log.d("LIST", "Unable to update list results as no list found");
		}
	}
}