package org.materialsproject.app;

import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.webkit.WebView;

public class DisplayAboutActivity extends Activity {
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WebView webView = new WebView(this);
		webView.loadUrl("file:///android_asset/www/about.html");
		setContentView(webView);
	}

}
