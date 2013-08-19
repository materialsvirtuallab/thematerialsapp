package org.materialsproject.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.text.Html;

public class MaterialsExplorer extends Activity {

	final Context context = this;
	MaterialsExplorer parent = this;

	public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";

	private Map<String, JSONObject> results = new TreeMap<String, JSONObject>();

	private Handler resultsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ListView listView = (ListView) findViewById(R.id.resultList);

			List<String> resultList = new ArrayList<String>();

			// Create ArrayAdapter using the planet list.
			ArrayAdapter listAdapter = new ArrayAdapter<String>(context,
					R.layout.matexplorer_result_row, resultList);

			listView.setAdapter(listAdapter);

			listAdapter.clear();

			for (String key : results.keySet()) {
				JSONObject subObj = results.get(key);
				String buttonText;
				try {
					buttonText = String.format(
							"Mid %s : %s (%s)",
							subObj.getString("material_id"),
							subObj.getString("full_formula").replaceAll(
									"(\\d+)", "<sub><tiny>$1</tiny></sub>"),
							subObj.getJSONObject("spacegroup").getString(
									"symbol"));
					listAdapter.add(Html.fromHtml(buttonText));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			final Object[] keys = results.keySet().toArray();

			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapterParent,
						View view, int position, long id) {
					try {
						Intent intent = new Intent(parent,
								DisplayResultsActivity.class);
						intent.putExtra(EXTRA_MESSAGE,
								results.get(keys[position]).toString());
						startActivity(intent);
					} catch (Exception ex) {

					}
				}
			});
			TextView msgView = (TextView) findViewById(R.id.message);
			if (msg.what == 0) {
				msgView.setText(String.format("%d result%s found.",
						results.size(), (results.size() == 1) ? "" : "s"));
			} else {
				msgView.setText(msg.getData().getString("error_msg"));
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_materials_explorer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

	/** Called when the user selects the Send button */
	public void doSearch(View view) {
		// Do something in response to button

		EditText editText = (EditText) findViewById(R.id.edit_message);
		final String message = editText.getText().toString();

		TextView msgView = (TextView) findViewById(R.id.message);

		class DownloadResultsTask extends AsyncTask<Handler, Void, Void> {

			@Override
			protected Void doInBackground(Handler... myhandler) {
				try {
					results.clear();

					String[] toks = message.split(",");
					for (String tok : toks) {
						String URL = MatProj.MP_URL + "materials/"
								+ tok.replaceAll("\\s+", "") + "/vasp?API_KEY="
								+ MatProj.API_KEY;
						String message = Utilities.makeHttpsRequest(URL);

						JSONObject obj = new JSONObject(message);
						final JSONArray response = obj.getJSONArray("response");

						for (int i = 0; i < response.length(); i++) {
							JSONObject subObj = response.getJSONObject(i);
							results.put(String.format("%.4f%s",
									subObj.getDouble("e_above_hull"),
									subObj.getString("material_id")), subObj);

						}
					}
					Message msg = Message.obtain();
					Bundle data = new Bundle();
					data.putString("error_msg", "none");
					msg.setData(data);
					myhandler[0].sendMessage(msg);
				} catch (Exception e) {
					Message msg = Message.obtain();
					Bundle data = new Bundle();
					data.putString("error_msg", e.toString());
					msg.setData(data);
					msg.what=1;
					myhandler[0].sendMessage(msg);
				}
				return null;
			}
		}

		msgView.setText("Getting results");
		new DownloadResultsTask().execute(resultsHandler);

	}

}
