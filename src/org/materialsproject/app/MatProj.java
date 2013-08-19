package org.materialsproject.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MatProj extends Activity {

	MatProj parent = this;
	public static final String PREFS_NAME = "MaterialsProjectPrefs";
	public static String API_KEY = "";
	//public static String MP_URL = "http://www.materialsproject.org:8080/rest/v1/";
	public static String MP_URL = "https://www.materialsproject.org/rest/v1/";
	
	public static int API_ATTEMPTS = 0;
	public static final String EXTRA_NEWS = "";
	private RssParser parser = null;
	private String errorMessage = "";
	
	
	Handler rssHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				parent.closeAlert("No network connectivity!",
						"The Materials Project App requires network connectivity!");
			} else if (msg.what == 2) {
				parent.getApiKey("Enter your API key","Please enter your API key. Get it at https://www.materialsproject.org/profile. You only need to do this once.");
			} else {
				final Context context = getApplicationContext();
				ListView rssListView = (ListView) findViewById(R.id.rssListView);
				ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(
						context, R.layout.rss_result_row, parser.allTitles);
				rssListView.setAdapter(listAdapter);
				rssListView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapterParent,
							View view, int position, long id) {
						try {
							Intent intent = new Intent(context,
									DisplayNewsActivity.class);
							String[] data = { parser.allTitles.get(position),
									parser.allContent.get(position) };
							intent.putExtra(EXTRA_NEWS, data);
							startActivity(intent);
						} catch (Exception ex) {

						}
					}
				});
			}
		}
	};
	
	
	Handler apiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				parent.getApiKey("Bad API key", "Please enter a valid API key.");
			} else {
				
			}
		}
	};
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_materials_project);

		class DownloadRSSTask extends AsyncTask<Handler, Void, Void> {

			@Override
			protected Void doInBackground(Handler... myhandler) {
				if (!parent.isNetworkAvailable()) {
					myhandler[0].sendEmptyMessage(1);
				}
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				API_KEY = settings.getString("API_KEY", "");
				if (API_KEY == "") {
					//parent.getApiKey("Enter your API key","Please enter your API key. You only need to do this once.");
					myhandler[0].sendEmptyMessage(2);
				}
				parser = new RssParser(
						"https://materialsproject.org/blog?feed=rss2");
				myhandler[0].sendEmptyMessage(0);
				return null;
			}
		}

		new DownloadRSSTask().execute(rssHandler);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Context context = this;
		switch (item.getItemId()) {
		case R.id.about:
			Intent intent = new Intent(context, DisplayAboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getApiKey(String title, String message) {
		
		class CheckApiKeyTask extends AsyncTask<Handler, Void, Void> {
			
			private String value;
			
			public CheckApiKeyTask(String value)
			{
				this.value = value;
			}

			@Override
			protected Void doInBackground(Handler... myhandler) {
				String url = MatProj.MP_URL + "api_check?API_KEY=" + this.value;
				String message = Utilities.makeHttpsRequest(url);
				try {
					JSONObject obj = new JSONObject(message);
					boolean valid_key = obj.getBoolean("api_key_valid");
					if (valid_key) {
						myhandler[0].sendEmptyMessage(0);
						API_KEY = this.value;
					} else {
						myhandler[0].sendEmptyMessage(1);
					}
				} catch (JSONException e) {
					//errorMessage = e.toString();
					myhandler[0].sendEmptyMessage(1);
				}
				return null;
			}
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final MatProj parent = this;
		alert.setTitle(title);
		alert.setMessage(message);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				new CheckApiKeyTask(value).execute(apiHandler);

			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						parent.closeAlert("Bad API key",
								"You need an API key to access the Materials Project.");
					}
				});

		alert.show();
	}

	private void closeAlert(String title, String message) {
		final Context context = this;
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Close App",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								MatProj.this.finish();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

	public void launchMaterialsExplorer(View view) {
		Intent intent = new Intent(this, MaterialsExplorer.class);
		startActivity(intent);
	}

	public void launchPhaseDiagram(View view) {
		Intent intent = new Intent(this, PhaseDiagram.class);
		startActivity(intent);
	}

	public void launchBatteryExplorer(View view) {
		Intent intent = new Intent(this, BatteryExplorer.class);
		startActivity(intent);

	}

	public void launchReactionCalculator(View view) {
		Intent intent = new Intent(this, ReactionCalculator.class);
		startActivity(intent);

	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	@Override
	protected void onStop() {
		super.onStop();

		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("API_KEY", API_KEY);

		// Commit the edits!
		editor.commit();
	}

}
