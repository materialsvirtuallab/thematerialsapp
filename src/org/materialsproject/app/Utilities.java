package org.materialsproject.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

public class Utilities {

	public static String makeHttpRequest(String url) {
		String message = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				message = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			message = "Client Protocol Exception : " + e.toString();
		} catch (IOException e) {
			message = "IO Exception : " + e.toString();
		}
		return message;
	}

	public static String makeHttpsRequest(String url) {
		String message = "";
		try {
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			DefaultHttpClient client = new DefaultHttpClient();
		    SchemeRegistry registry = new SchemeRegistry();
		    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		    socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		    registry.register(new Scheme("https", socketFactory, 443));
		    SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		    DefaultHttpClient httpclient = new DefaultHttpClient(mgr, client.getParams());

		    // Set verifier     
		    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			
			HttpResponse response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				message = out.toString();
			} else {
				// Closes the connection.
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				message = out.toString();
				//response.getEntity().getContent().close();
				//throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			message = "Client Protocol Exception : " + e.toString();
		} catch (IOException e) {
			message = "IO Exception : " + e.toString();
		}
		return message;
	}
	
}
