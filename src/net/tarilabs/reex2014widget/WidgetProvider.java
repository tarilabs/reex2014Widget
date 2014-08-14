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
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	 
	  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	    for (int i = 0; i < appWidgetIds.length; i++) {
	      int appWidgetId = appWidgetIds[i];
	      
	      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
	      
	      /*
	      views.setTextViewText(R.id.cogitoLabel, (new SimpleDateFormat("hh:mm:ss", Locale.getDefault())).format(new Date()));
 
	      Intent intent = new Intent(context, WidgetProvider.class);
	      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
	      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	      
	      That was all about setting a widget text and tap-to-update, no 3 lines below.
	      */
	      
	      (new FetchCogitoTask(views, appWidgetId, appWidgetManager)).execute("http://192.168.0.13:8080/reex2014/restapi/ruleengine/query/cogitoergosum");
	      
	      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
	      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	      
	      views.setOnClickPendingIntent(R.id.cogitoLabel, pendingIntent);
	      appWidgetManager.updateAppWidget(appWidgetId, views);
	    }
	  }
	  
	private class FetchCogitoTask extends AsyncTask<String, Void, String> {
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
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				// REST api first is array of Drools Query
				JSONArray jsonArray = new JSONArray(response);
				// this Drools business query result 1st slot is object row of cogito and $text
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				// get the $text
				response = jsonObject.getString("$text");
			} catch (Exception e) {
				e.printStackTrace();
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
}
