package com.github.teenhack42.cruisescanner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.github.teenhack42.CruiseScanner;

import java.util.ArrayList;

public class TicketSearch extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_search);

		new loadList(CruiseScanner.getAppContext(), findViewById(android.R.id.content)).execute();
	}

	@Override
	protected void onResume() {
		super.onResume();

		new loadList(CruiseScanner.getAppContext(), findViewById(android.R.id.content)).execute();
// 1



	}
}

class loadList extends AsyncTask<Void, Integer, ArrayList<Ticket>> {

	private Context mContext;
	private View rootView;

	public loadList(Context context, View rootView) {
		this.mContext=context;
		this.rootView=rootView;
	}

	@Override
	protected ArrayList<Ticket> doInBackground(Void... voids) {
		final ArrayList<Ticket> ticketList = Ticket.getTicketsFromServer();
		return ticketList;
	}

	@Override
	protected void onPostExecute(ArrayList<Ticket> tickets) {
		ListView mListView = (ListView) rootView.findViewById(R.id.searchResults);
		TicketAdapter adapter = new TicketAdapter(this.mContext, tickets);

		mListView.setAdapter(adapter);
	}
}