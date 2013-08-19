package org.materialsproject.app;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PhaseDiagram extends Activity {
	final Context context = this;
	public final static String EXTRA_CHEMSYS = "CHEMSYS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_phase_diagram);
		ListView elListView = (ListView) findViewById(R.id.elListView);
		Resources r = getResources();
		String[] listItems = r.getStringArray(R.array.el_array);
		elListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, listItems));
		elListView.setItemsCanFocus(false);
		elListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	private List<String> getCheckedElements() {
		ListView elListView = (ListView) findViewById(R.id.elListView);
		SparseBooleanArray checked = elListView.getCheckedItemPositions();
		int len = elListView.getCount();
		List<String> els = new ArrayList<String>();
		for (int i = 0; i < len; i++)
			if (checked.get(i)) {
				String item = (String) elListView.getItemAtPosition(i);
				els.add(item.split("-")[1].trim());
			}
		return els;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

	public void getPhaseDiagram(View view) {

		List<String> els = getCheckedElements();
		String chemsys = "";
		int numEls = els.size();
		for (String el : els) {
			chemsys += el + "-";
		}
		TextView textView = (TextView) findViewById(R.id.message);
		if (numEls > 1 && numEls < 4) {
			Intent intent = new Intent(this, DisplayPhaseDiagramActivity.class);
			intent.putExtra(EXTRA_CHEMSYS,
					chemsys.substring(0, chemsys.length() - 1));
			startActivity(intent);
		} else if (numEls < 2) {
			textView.setText("Insufficient elements selected!");
		} else {
			textView.setText("Too many elements selected!");
		}
		
	}

}
