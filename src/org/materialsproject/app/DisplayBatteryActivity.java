package org.materialsproject.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DisplayBatteryActivity extends Activity {

	private Bitmap voltageProfileImage = null;

	private Handler voltageProfileImageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ImageView i = (ImageView) findViewById(R.id.voltageProfileImage);
			i.setImageBitmap(voltageProfileImage);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.activity_battery_details);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.ic_launcher);
		Intent intent = getIntent();
		String message = intent.getStringExtra(MaterialsExplorer.EXTRA_MESSAGE);
		// Create the text view
		TableLayout table = (TableLayout) findViewById(R.id.detailsTable);

		try {

			JSONObject subObj = new JSONObject(message);

			double maxFrac = subObj.getDouble("max_frac");
			double minFrac = subObj.getDouble("min_frac");
			int numSites = subObj.getInt("numsites");
			String formula = subObj.getString("reduced_cell_formula")
					.replaceAll("(\\d+)", "<sub><tiny>$1</tiny></sub>");

			double minLi = minFrac * numSites / (1 - minFrac);
			double maxLi = maxFrac * numSites / (1 - maxFrac);

			// double minLi = 1;
			// double maxLi = 2;
			DecimalFormat decFormat = new DecimalFormat("#.##");
			formula = String.format("Li<sub><tiny>%s-%s</tiny></sub>%s",
					decFormat.format(minLi), decFormat.format(maxLi), formula);

			TextView titleText = (TextView) findViewById(R.id.titleText);
			titleText.setText(String.format("Battery %s",
					subObj.getString("battid")));
			titleText.setTextColor(Color.parseColor("#fe6a00"));

			this.insertRow(table, "Formula", formula);
			JSONObject spacegroup = subObj.getJSONObject("spacegroup");

			this.insertRow(table, "Crystal type",
					spacegroup.getString("crystal_system"));
			this.insertRow(table, "Spacegroup", spacegroup.getString("symbol"));
			this.insertRow(
					table,
					"Int. Number",
					Integer.toString(subObj.getJSONObject("spacegroup").getInt(
							"number")));

			this.insertRow(table, "Average voltage", String.format("%.2f V",
					subObj.getDouble("average_voltage")));

			this.insertRow(
					table,
					"Grav. capacity",
					String.format("%.0f mAh g<sup-1</sup>",
							subObj.getDouble("capacity_grav")));
			this.insertRow(
					table,
					"Vol. capacity",
					String.format("%.0f Ah l<sup>-1</sup>",
							subObj.getDouble("capacity_vol")));
			this.insertRow(
					table,
					"Specific energy",
					String.format("%.0f Wh g<sup-1</sup>",
							subObj.getDouble("energy_grav")));
			this.insertRow(
					table,
					"Energy density",
					String.format("%.0f Wh l<sup>-1</sup>",
							subObj.getDouble("energy_vol")));
			this.insertRow(table, "Voltage profile", "Please scroll down");

			final String imageUrl = String.format(MatProj.MP_URL
					+ "battery/%s/voltage_profile?API_KEY=%s", subObj.getString("battid"),
					MatProj.API_KEY);

			class DownloadVoltageProfileTask extends
					AsyncTask<Handler, Void, Void> {

				@Override
				protected Void doInBackground(Handler... myhandler) {
					try {

						voltageProfileImage = BitmapFactory
								.decodeStream((InputStream) new URL(imageUrl)
										.getContent());
						myhandler[0].sendEmptyMessage(1);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			}

			new DownloadVoltageProfileTask()
					.execute(voltageProfileImageHandler);

		} catch (Exception e) {

		}

	}

	public void insertRow(TableLayout table, String header, String value) {
		int count = table.getChildCount();
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(params);

		TextView textview = new TextView(this);
		textview.setText(header);
		textview.setTypeface(null, Typeface.BOLD);
		textview.setTextSize(16);
		textview.setPadding(10, 10, 10, 10);
		textview.setLineSpacing(3, 1);
		textview.setBackgroundColor((count % 2 == 0) ? Color
				.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
		tr.addView(textview, cellLp);

		textview = new TextView(this);
		textview.setText(Html.fromHtml(value));
		textview.setTextSize(16);
		textview.setPadding(10, 10, 10, 10);
		textview.setLineSpacing(3, 1);
		textview.setBackgroundColor((count % 2 == 0) ? Color
				.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
		tr.addView(textview, cellLp);

		table.addView(tr, new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT));
	}

	public static String getHtmlFormula(String normalFormula) {
		return normalFormula.replaceAll("(\\d+)",
				"<sub><small>$1</small></sub>");
	}

}