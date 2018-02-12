package com.github.teenhack42.cruisescanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.teenhack42.CruiseScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TicketSearch extends AppCompatActivity {

	private ListView mListView;

	private SwipeRefreshLayout mSwipeRefresh;

	private Button searchButton;
	private EditText searchArea;

	ArrayList<Ticket> ticketList;
	TicketAdapter adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_search);

		mListView = findViewById(R.id.ticket_list);
		mSwipeRefresh = findViewById(R.id.swiperefreshTickets);

		mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshTicketList();
			}
		});
// 1
		int ticketDlTimeStart = (int) System.currentTimeMillis();
		ticketList = Ticket.downloadTickets();
		int ticketDLTime = (int) (System.currentTimeMillis() - ticketDlTimeStart);

		Log.d("TicketDownloadTime", String.valueOf(ticketDLTime));
// 2
		adapter = new TicketAdapter(this, ticketList);
		mListView.setAdapter(adapter);


		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 1
				Ticket sTicket = ticketList.get(position);

				// 2
				Intent viewIntent = new Intent(CruiseScanner.getAppContext(), TicketView.class);

				// 3
				viewIntent.putExtra("uid", sTicket.uid);

				// 4
				startActivity(viewIntent);
			}

		});
	}


	//for the swipe refresh

	public void refreshTicketList() {
		mSwipeRefresh.setRefreshing(true);
		ticketList.clear();

		ExecutorService executor = Executors.newFixedThreadPool(1);
		final Future<ArrayList<Ticket>> result = executor.submit(new Callable<ArrayList<Ticket>>() {
			@Override
			public ArrayList<Ticket> call() throws Exception {
				return Ticket.downloadTickets();
			}
		});
		try {
			ArrayList<Ticket> value = result.get();
			for (Ticket t : value) {
				ticketList.add(t);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
			}
		});
		mSwipeRefresh.setRefreshing(false);
		executor.shutdown();
	}

}