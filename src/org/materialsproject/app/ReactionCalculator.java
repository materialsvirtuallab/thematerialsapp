package org.materialsproject.app;

import java.util.Iterator;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.text.Html;

public class ReactionCalculator extends Activity {

	final Context context = this;
	public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";
	public final static double EV_TO_KJ_MOL = 96.4853365;
	ReactionCalculator parent = this;
	private JSONObject response;

	private Handler resultsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TableLayout table = (TableLayout) findViewById(R.id.reactionTable);
			TextView msgView = (TextView) findViewById(R.id.message);
			if (msg.what == 0) {
				try {
					if (response.getBoolean("Valid_reaction")) {
						TextView titleText = (TextView) findViewById(R.id.reactionString);
						titleText.setText(Html.fromHtml(response.getString(
								"Pretty_normalized_reaction").replaceAll(
								"([A-Za-z]+)(\\d+)",
								"$1<sub><small>$2</small></sub>")));
						titleText.setTextColor(Color.parseColor("#fe6a00"));
						String calcEnergy = String.format("%.3f",
								response.getDouble("Reaction_energy"));
						String expEnergy = "--";
						if (response.has("Experimental_reaction_energy")) {
							expEnergy = String.format("%.3f", response
									.getDouble("Experimental_reaction_energy"));
						}
						String[] header = { "Calc.", "Expt." };

						parent.insertRow(table, "", header);

						String[] energies = { calcEnergy, expEnergy };

						parent.insertRow(table, "Rxn energy (eV)", energies);

						calcEnergy = String.format("%.0f",
								response.getDouble("Reaction_energy")
										* EV_TO_KJ_MOL);
						expEnergy = "--";
						if (response.has("Experimental_reaction_energy")) {
							expEnergy = String
									.format("%.0f",
											response.getDouble("Experimental_reaction_energy")
													* EV_TO_KJ_MOL);
						}
						String[] expEnergies = { calcEnergy, expEnergy };
						parent.insertRow(table, "Rxn energy (kJ/mol)",
								expEnergies);

						String[] noneData = { "", "" };
						parent.insertRow(table, "Form. energies", noneData);

						JSONObject subObj = response
								.getJSONObject("Calculated_formation_energies");
						JSONObject expObj = response
								.getJSONObject("Experimental_references");

						Iterator<String> formulas = subObj.keys();
						while (formulas.hasNext()) {
							String formula = formulas.next();
							String[] formE = { "", "--" };
							String[] formEKJ = { "", "--" };

							formE[0] = String.format("%.3f",
									subObj.getDouble(formula));
							formEKJ[0] = String.format("%.0f",
									subObj.getDouble(formula) * EV_TO_KJ_MOL);
							if (expObj.has(formula)) {
								double expFormE = expObj.getJSONObject(formula)
										.getDouble("Formation_energy");
								formE[1] = String.format("%.3f", expFormE
										/ EV_TO_KJ_MOL);
								formEKJ[1] = String.format("%.0f", expFormE);
							}
							parent.insertRow(table, String.format(
									"H<sub>f</sub> %s (eV)",
									formula.replaceAll("([A-Za-z]+)(\\d+)",
											"$1<sub><small>$2</small></sub>")),
									formE);
							parent.insertRow(table, String.format(
									"H<sub>f</sub> %s (kJ/mol)",
									formula.replaceAll("([A-Za-z]+)(\\d+)",
											"$1<sub><small>$2</small></sub>")),
									formEKJ);
						}

						msgView.setText("");
					} else {
						msgView.setText(response.getString("Error_message"));
					}
				} catch (Exception ex) {
					msgView.setText(ex.toString());
				}
			} else {
				msgView.setText(msg.getData().getString("error_msg"));
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_reaction_calculator);

		TextView textview = (TextView) findViewById(R.id.rightArrow);
		textview.setText(Html.fromHtml("&rarr;"));
		/*
		 * textview = (TextView) findViewById(R.id.reactantsTitle);
		 * textview.setText("Reactants"); textview = (TextView)
		 * findViewById(R.id.productsTitle); textview.setText("Products");
		 * textview = (TextView) findViewById(R.id.space);
		 * textview.setText(" ");
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

	/** Called when the user selects the Send button */
	public void calculateReaction(View view) {
		// Do something in response to button

		EditText editText = (EditText) findViewById(R.id.reactants);
		String reactants = editText.getText().toString();
		editText = (EditText) findViewById(R.id.products);
		String products = editText.getText().toString();

		String[] reactantsArray = reactants.split("[^A-Za-z0-9\\s]+");
		String[] productsArray = products.split("[^A-Za-z0-9\\s]+");

		StringBuilder reactantStr = new StringBuilder();
		for (String rct : reactantsArray) {
			reactantStr.append(rct.replaceAll("\\s+", ""));
			reactantStr.append(",");
		}
		StringBuilder productStr = new StringBuilder();
		for (String prd : productsArray) {
			productStr.append(prd.replaceAll("\\s+", ""));
			productStr.append(",");
		}

		reactantStr.deleteCharAt(reactantStr.length() - 1);
		productStr.deleteCharAt(productStr.length() - 1);

		final TextView msgView = (TextView) findViewById(R.id.message);

		final String URL = MatProj.MP_URL + "reaction/" + reactantStr.toString() + "-"
				+ productStr.toString() + "?API_KEY="
				+ MatProj.API_KEY;

		class DownloadResultsTask extends AsyncTask<Handler, Void, Void> {

			@Override
			protected Void doInBackground(Handler... myhandler) {
				String message = Utilities.makeHttpsRequest(URL);

				try {
					JSONObject obj = new JSONObject(message);
					response = obj.getJSONObject("reaction");
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
					msg.what = 1;
					myhandler[0].sendMessage(msg);
				}
				return null;
			}
		}
		msgView.setText("Calculating reaction...");
		new DownloadResultsTask().execute(resultsHandler);
	}

	public void insertRow(TableLayout table, String header, String[] values) {
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
		textview.setText(Html.fromHtml(header));
		textview.setTypeface(null, Typeface.BOLD);
		textview.setTextSize(16);
		textview.setPadding(10, 10, 10, 10);
		textview.setBackgroundColor((count % 2 == 0) ? Color
				.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
		tr.addView(textview, cellLp);

		for (String value : values) {
			textview = new TextView(this);
			textview.setText(Html.fromHtml(value));
			textview.setTextSize(16);
			textview.setPadding(10, 10, 10, 10);
			textview.setBackgroundColor((count % 2 == 0) ? Color
					.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
			tr.addView(textview, cellLp);
		}
		table.addView(tr, new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT));
	}

}
