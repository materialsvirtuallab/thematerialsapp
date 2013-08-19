package org.materialsproject.app;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DisplayResultsActivity extends Activity {
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_materials_project, menu);
        return true;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_materials_details);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);
		Intent intent = getIntent();
		String message = intent.getStringExtra(MaterialsExplorer.EXTRA_MESSAGE);
		// Create the text view
		TableLayout table = (TableLayout)findViewById(R.id.detailsTable);
		
		try {
			
			JSONObject subobj = new JSONObject(message);
						
			TextView titleText = (TextView)findViewById(R.id.titleText);
			titleText.setText(String.format("Material %s", subobj.getString("material_id")));
			this.insertRow(table, "Formula", getHtmlFormula(subobj.getString("pretty_formula")));
			this.insertRow(table, "Unit cell formula", getHtmlFormula(subobj.getString("full_formula")));
			JSONObject spacegroup = subobj.getJSONObject("spacegroup");
			
			this.insertRow(table, "Crystal type", spacegroup.getString("crystal_system"));
			this.insertRow(table, "Spacegroup", spacegroup.getString("symbol"));
			this.insertRow(table, "Int. Number", Integer.toString(subobj.getJSONObject("spacegroup").getInt("number")));
			this.insertRow(table, "Energy / atom", String.format("%.3f eV", subobj.getDouble("energy_per_atom")));
			
			if (subobj.getBoolean("is_compatible")) {
				this.insertRow(table, "Form. energy / atom", String.format("%.3f eV", subobj.getDouble("formation_energy_per_atom")));
				this.insertRow(table, "Energy above hull", String.format("%.3f eV", subobj.getDouble("e_above_hull")));
			}
			this.insertRow(table, "Final density", String.format("%.3f g cm<sup><small>-3</small></sup>", subobj.getDouble("density")));
			
			try{
				JSONObject bsObj = subobj.getJSONObject("bandstructure.band_gap");
				this.insertRow(table, "Band Gap", String.format("%.3f eV", bsObj.getDouble("energy")));
				
				if (bsObj.getDouble("energy") > 0)
				{
					String bgType = bsObj.getBoolean("direct") ? "Direct" : "Indirect";
					this.insertRow(table, "Bandgap transition", bgType + " " + bsObj.getString("transition").replace("\\Gamma", "&Gamma;"));
				}
			}
			catch (Exception ex)
			{
				
			}
			String runType = (subobj.getBoolean("is_hubbard")) ? "GGA+U" : "GGA";
			
			this.insertRow(table, "Run type", runType);
		} catch (Exception e) {

		}
	
	}
	
	public void insertRow(TableLayout table, String header, String value)
	{
		int count = table.getChildCount();
		TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(params);
	    
		TextView textview = new TextView(this);
		textview.setText(header);
		textview.setTypeface(null, Typeface.BOLD);
		textview.setTextSize(16);
		textview.setPadding(10, 10, 10, 10);
		textview.setBackgroundColor((count % 2 == 0) ? Color.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
		tr.addView(textview, cellLp);
	    
		textview = new TextView(this);
		textview.setText(Html.fromHtml(value));
		textview.setTextSize(16);
		textview.setPadding(10, 10, 10, 10);
		textview.setBackgroundColor((count % 2 == 0) ? Color.parseColor("#cccccc") : Color.parseColor("#eeeeee"));
		tr.addView (textview, cellLp);
		
		table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
	}
	
	public static String getHtmlFormula(String normalFormula)
	{
		return normalFormula.replaceAll("(\\d+)", "<sub><small>$1</small></sub>");
	}
	
}