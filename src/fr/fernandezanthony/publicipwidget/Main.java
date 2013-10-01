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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Main extends AppWidgetProvider  {

	private String ipAddress = "searching...";
	private Context mcontext;
	private DataBaseHelper myDbHelperAccount;
	private SQLiteDatabase myDbAccount = null;

	private AppWidgetManager appWidgetManagerTemp;
	private int[] appWidgetIdsTemp;

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
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		mcontext = context;
		appWidgetManagerTemp = appWidgetManager;
		appWidgetIdsTemp = appWidgetIds;
		ipAddress = getip(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
		remoteViews.setTextViewText(R.id.textView1, "Public IP - "+ ipAddress);

		Intent intent = new Intent(WIDGET_BUTTON);
		intent.putExtra("ip", ipAddress);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.imageButton1, pendingIntent );

		appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);

		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId, getip(context)
					);
		}
			
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mcontext = context;
			if (WIDGET_BUTTON.equals(intent.getAction())) {
				updateMethod();
				ipAddress = getip(context);
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE); 
				clipboard.setText(getip(context)); 
				Toast.makeText(context.getApplicationContext(), "Public IP copied to clipboard", Toast.LENGTH_SHORT).show();

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Main.class.getName());
				int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

				onUpdate(context, appWidgetManager, appWidgetIds);
			}
			super.onReceive(context, intent);
		}

		public void updateMethod() {
			CAsyncTask task = new CAsyncTask();
			task.execute();
		}

		public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
				int appWidgetId, String ip){
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_main);
			remoteViews.setTextViewText(R.id.textView1, "Public IP - "+ ip);

			appWidgetManager.updateAppWidget(appWidgetId,remoteViews);

			//Toast.makeText(context, "updateAppWidget(): " + String.valueOf(appWidgetId), Toast.LENGTH_LONG).show();

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
					HttpGet httpget = new HttpGet("http://ip.jsontest.com/");
					//HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
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
				return listip.get(listip.size()-1).getIp();

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
