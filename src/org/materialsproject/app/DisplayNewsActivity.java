package org.materialsproject.app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.webkit.WebView;

public class DisplayNewsActivity extends Activity {
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		String[] data = intent.getStringArrayExtra(MatProj.EXTRA_NEWS);
		WebView webView = new WebView(this);
		webView.loadData("<html><body><h3>" + data[0] + "</h3>"+data[1]+"</body></html>", "text/html", "utf-8");// .loadUrl("file:///android_asset/www/latest_news.html");

		setContentView(webView);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_materials_project, menu);
		return true;
	}

}
