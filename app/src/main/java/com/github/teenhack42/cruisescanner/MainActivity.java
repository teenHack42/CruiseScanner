package com.github.teenhack42.cruisescanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.teenhack42.CruiseScanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class MainActivity extends Activity {

	// QREader
	private SurfaceView mySurfaceView;
	private QREader qrEader;

	private String qr_string = "";
	private Long qrSoundTime = System.currentTimeMillis();
	private Long qrCoolTime = System.currentTimeMillis();

	private TextView text;
	private TextView paid_text;

	private RadioGroup scan_type_group;
	private int scan_type;

	private ArrayList<Ticket> scanned_tickets;

	public Hook hook = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		Context context = CruiseScanner.getAppContext();

		if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
				== PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
		}

		try {
			hook = new Hook("http://192.16.0.12/api/hook");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Toast.makeText(CruiseScanner.getAppContext(), "Server Address Invalid", Toast.LENGTH_LONG);
		}

		qrSoundTime = System.currentTimeMillis();

		text = findViewById(R.id.ticket_uid);
		paid_text = findViewById(R.id.paid_bool);

		scan_type_group = findViewById(R.id.scanType);
		scan_type = scan_type_group.getCheckedRadioButtonId();
		scan_type_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int i) {
				scan_type = scan_type_group.getCheckedRadioButtonId();
			}
		});


		final FloatingActionButton searchTicketViewButton = findViewById(R.id.floatingActionShowSearch);
		searchTicketViewButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, TicketSearch.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		// Setup SurfaceView
		// -----------------
		mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);

		final MediaPlayer sound_ding_up = MediaPlayer.create(MainActivity.this, R.raw.ding_up);

		// Init QREader
		// ------------
		qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
			@Override
			public void onDetected(final String data) {

				if ((System.currentTimeMillis() - qrCoolTime) > 800) {
					qrCoolTime = System.currentTimeMillis();

					String[] dataSplit = data.split(":");
					if (!(dataSplit[0].equals("HC2018"))) {
						return;
					}

					switch (dataSplit[1]) {
						case "Ticket":
							sound_ding_up.start();
							String uid = dataSplit[2];
							switch (scan_type) {
								case R.id.radio_view:
									Intent myIntent = new Intent(MainActivity.this, TicketView.class);
									myIntent.putExtra("uid", uid);
									MainActivity.this.startActivity(myIntent);
									break;

								case R.id.radio_checkin:
									new setAttendance(MainActivity.this).execute(new Attendance(uid, true));
									MainActivity.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											paid_text.setText("IN");
											paid_text.postDelayed(new Runnable() {
												@Override
												public void run() {
													paid_text.setText("");
												}
											}, 500);
										}
									});
									break;

								case R.id.radio_checkout:
									new setAttendance(MainActivity.this).execute(new Attendance(uid, false));
									MainActivity.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											paid_text.setText("OUT");
											paid_text.postDelayed(new Runnable() {
												@Override
												public void run() {
													paid_text.setText("");
												}
											}, 500);
										}
									});

									break;
							}
							break;
					}
				}
			}
		}).facing(QREader.BACK_CAM).enableAutofocus(true).height(mySurfaceView.getHeight()).width(mySurfaceView.getWidth()).build();


		if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
			qrEader.start();
		}
	}


	@Override
	protected void onResume() {
		super.onResume();

		// Init and Start with SurfaceView
		// -------------------------------
		qrEader.initAndStart(mySurfaceView);
		qrEader.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Cleanup in onPause()
		// --------------------
		qrEader.releaseAndCleanup();
		qrEader.stop();
	}
}
