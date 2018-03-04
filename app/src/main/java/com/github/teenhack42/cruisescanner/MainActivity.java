package com.github.teenhack42.cruisescanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.teenhack42.CruiseScanner;
import com.github.teenhack42.cruisescanner.Hook;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

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
			hook = new Hook("http://192.168.0.12:3000/app/hook");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Toast.makeText(CruiseScanner.getAppContext(), "Server Address Invalid", Toast.LENGTH_LONG);
		}

		qrSoundTime = System.currentTimeMillis();

		text = (TextView) findViewById(R.id.ticket_uid);
		paid_text = (TextView) findViewById(R.id.paid_bool);

		final FloatingActionButton searchTicketViewButton = (FloatingActionButton) findViewById(R.id.floatingActionShowSearch);
		searchTicketViewButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(MainActivity.this, TicketSearch.class);
				MainActivity.this.startActivity(myIntent);
			}
		});

		// Setup SurfaceView
		// -----------------
		mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);

		// Init QREader
		// ------------
		qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
			@Override
			public void onDetected(final String data) {

				if ((System.currentTimeMillis() - qrCoolTime) > 500) {
					qrCoolTime = System.currentTimeMillis();


					String[] dataSplit = data.split(":");
					if (!(dataSplit[0].equals("HC2018"))) {
						return;
					}

					switch (dataSplit[1]) {
						case "Ticket":
							Intent myIntent = new Intent(MainActivity.this, TicketView.class);
							myIntent.putExtra("uid", dataSplit[2]);
							MainActivity.this.startActivity(myIntent);
							//checkOffTicket(dataSplit[2]);
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

	public void checkOffTicket(final String uid) {

		if (((System.currentTimeMillis() - qrSoundTime) > 1500) || (((System.currentTimeMillis() - qrSoundTime) < 1500) && (!qr_string.equals(uid)))) {
			try {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(CruiseScanner.getAppContext(), notification);
				r.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
			qrSoundTime = System.currentTimeMillis();


			//if (!qr_string.equals(uid)) {
			Log.d("QREader", "Value : " + uid + " QR string: " + qr_string);
			qr_string = uid;

			MainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					text.setText(uid);

					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("uid", uid);


					new CheckOffTicket().execute(params);
				}
			});
			//}
		}
	}


	class CheckOffTicket extends AsyncTask<HashMap<String, Object>, Integer, String> {
		protected String doInBackground(HashMap<String, Object>... params) {
			return hook.post("checkin", params[0]);
		}

		protected void onPostExecute(String result) {
			JSONObject json = null;

			boolean valid = false;
			boolean paid = false;
			boolean allow = false;

			try {
				json = new JSONObject(result);
				valid = json.getBoolean("valid");
				paid = json.getBoolean("paid");
				allow = json.getBoolean("allow");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			text.setText("valid: " + valid + " allow: " + allow);
			paid_text.setText(paid ? "PAID" : "PLEASE PAY");
			paid_text.postDelayed(new Runnable() {
				@Override
				public void run() {
					paid_text.setText("");
				}
			}, 4000);

		}
	}
}
