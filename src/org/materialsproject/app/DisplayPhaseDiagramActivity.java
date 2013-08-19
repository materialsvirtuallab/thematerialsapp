package org.materialsproject.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayPhaseDiagramActivity extends Activity {

	private Bitmap pdImage = null;

	private Handler pdHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ImageView i = (ImageView) findViewById(R.id.pdImage);
			i.setImageBitmap(pdImage);
			TextView textView = (TextView) findViewById(R.id.pdMessage);
			textView.setText("");
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
		setContentView(R.layout.activity_show_phase_diagram);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.ic_launcher);
		Intent intent = getIntent();
		String chemsys = intent.getStringExtra(PhaseDiagram.EXTRA_CHEMSYS);
		TextView textView = (TextView) findViewById(R.id.pdTitle);
		textView.setText(chemsys + " phase diagram");
		textView = (TextView) findViewById(R.id.pdMessage);
		textView.setText("Downloading phase diagram... please wait");
		
		final String imageUrl = String.format(MatProj.MP_URL
				+ "phase_diagram/%s?API_KEY=%s", chemsys, MatProj.API_KEY);

		class DownloadPDImageTask extends AsyncTask<Handler, Void, Void> {

			@Override
			protected Void doInBackground(Handler... myhandler) {
				try {

					pdImage = BitmapFactory.decodeStream((InputStream) new URL(
							imageUrl).getContent());
					myhandler[0].sendEmptyMessage(1);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}

		new DownloadPDImageTask().execute(pdHandler);
	}

}