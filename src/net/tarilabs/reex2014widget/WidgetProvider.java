package net.tarilabs.reex2014widget;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	public static void updateAppWidget(Context context,	AppWidgetManager appWidgetManager, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(Configure.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		String cogitoBaseURL = prefs.getString(Configure.SHARED_PREF_KEY, "asd");
		Log.i("net.tarilabs", "cogito base URL from prefs: " + cogitoBaseURL);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

		FetchCogitoTask asyncTask = new FetchCogitoTask(views, appWidgetId, appWidgetManager);
		asyncTask.execute(cogitoBaseURL + "/restapi/ruleengine/query/cogitoergosum");

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cogitoBaseURL + "/explore.xhtml#cogitoergosum"));
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		views.setOnClickPendingIntent(R.id.cogitoLabel, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}

class FetchCogitoTask extends AsyncTask<String, Void, String> {
	private RemoteViews views;
	private int widgetID;
	private AppWidgetManager appWidgetManager;

	public FetchCogitoTask(RemoteViews views, int appWidgetID,
			AppWidgetManager appWidgetManager) {
		this.views = views;
		this.widgetID = appWidgetID;
		this.appWidgetManager = appWidgetManager;
	}

	@Override
	protected String doInBackground(String... urls) {
		String response = "";
		String url = urls[0];
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse execute = client.execute(httpGet);
			InputStream content = execute.getEntity().getContent();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			StringBuilder rBuilder = new StringBuilder();
			while ((s = buffer.readLine()) != null) {
				rBuilder.append(s);
			}
			// REST api first is array of Drools Query
			JSONArray jsonArray = new JSONArray(rBuilder.toString());
			// this Drools business query result 1st slot is object row of cogito and $text
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			// get the $text
			response = jsonObject.getString("$text");
		} catch (Exception e) {
			Log.e("net.tarilabs", "Something wrong while asyncTask: "+e.getMessage(), e);
			response = "Connection error.";
		}
		return response;
	}

	@Override
	protected void onPostExecute(String result) {
		if (isCancelled()) {
			views.setTextViewText(R.id.cogitoLabel, "ERROR task cancelled?");
	    } else {
	    	views.setTextViewText(R.id.cogitoLabel, result);
	    }
		appWidgetManager.updateAppWidget(widgetID, views);
	}
}
