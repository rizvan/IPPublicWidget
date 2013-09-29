package fr.fernandezanthony.publicipwidget;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class Main extends AppWidgetProvider  {

	private String ipAddress = "searching...";
	private Context mcontext;
	private DataBaseHelper myDbHelperAccount;
	private SQLiteDatabase myDbAccount = null;

	public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
	public static String WIDGET_BUTTON = "fr.fernandezanthony.publicipwidget.WIDGET_BUTTON";

	@Override
	public void onEnabled(Context context) {
		mcontext = context;
		updateMethod();
		ipAddress = getip(context);
		super.onEnabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		mcontext = context;
		//updateMethod();
		ipAddress = getip(context);
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
		remoteViews.setTextViewText(R.id.textView1, "Public IP - "+ ipAddress);

		Intent intent = new Intent(WIDGET_BUTTON);
		intent.putExtra("ip", ipAddress);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.imageButton1, pendingIntent );

		appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mcontext = context;
		if (WIDGET_BUTTON.equals(intent.getAction())) {
			updateMethod();
			ipAddress = getip(context);
			Log.w("tag", "Button click");
		}
		super.onReceive(context, intent);
	}
	
	public void updateMethod() {
		CAsyncTask task = new CAsyncTask();
		task.execute();
	}

	private class CAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground (String... pars) {
			String ip = null;
			String str;
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
				// HttpGet httpget = new HttpGet("http://whatismyip.com.au/");
				// HttpGet httpget = new HttpGet("http://www.whatismyip.org/");
				HttpResponse response;

				response = httpclient.execute(httpget);
				//Log.i("externalip",response.getStatusLine().toString());

				HttpEntity entity = response.getEntity();
				entity.getContentLength();
				str = EntityUtils.toString(entity);
				JSONObject json_data = new JSONObject(str);
				ip = json_data.getString("ip");
				return ip;
			}
			catch (Exception e){
				Log.e("error", "Error"+e.toString());
			}
			return ip;
		} // doInBackground ();

		protected void onPostExecute (String retour) {
			ipAddress = retour;
			insert(retour);
			Log.w("tag", "IP = "+retour);
		} // onPostExecute ();

	} // CAsyncTask ();

	public String getip(Context context) {
		//Account retrieving
		myDbHelperAccount = new DataBaseHelper(context);

		try {
			// A reference to the database can be obtained after initialization.
			myDbAccount = myDbHelperAccount.getWritableDatabase();

			List<IP> listip = myDbHelperAccount.getAllAccount();
			Log.w("tag", "CURSOR size = "+listip.size());
			return listip.get(0).getIp();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				myDbHelperAccount.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				myDbAccount.close();
			}
		}
		return null;
	}


	public void insert(String tempIP) {
		Log.w("tag", "TEMPIP = "+tempIP);
		//Account retrieving
		myDbHelperAccount = new DataBaseHelper(mcontext);

		try {
			// A reference to the database can be obtained after initialization.
			myDbAccount = myDbHelperAccount.getWritableDatabase();
			myDbHelperAccount.deleteAll();
			
			IP ip = new IP();
			ip.setIp(tempIP);

			myDbHelperAccount.addAccount(ip);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				myDbHelperAccount.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				myDbAccount.close();
			}
		}
	}
}
