package com.test.imagelist;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.imagelist.InstagramApp.OAuthAuthenticationListener;

public class MainActivity extends Activity {

	InstagramApp mApp;
	private Button btnConnect;
	private TextView tvSummary;
	private ProgressBar mLoadingPb;
	private GridView mGridView;
	InstagramSession mInstagramSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		mApp = new InstagramApp(this, Constants.CLIENT_ID,
				Constants.CLIENT_SECRET, Constants.CALLBACK_URL);

		mApp.setListener(listener);

		mInstagramSession	= mApp.getSession();

		if (mApp.hasAccessToken()) {
			setContentView(R.layout.activity_user);
			mLoadingPb 	= (ProgressBar) findViewById(R.id.pb_loading);
			mGridView	= (GridView) findViewById(R.id.gridView);
			((TextView) findViewById(R.id.tv_name)).setText(mApp.getUserName());
			((TextView) findViewById(R.id.tv_username)).setText(mApp.getName());
			new DownloadTask().execute();

		}else{
			setContentView(R.layout.activity_main);
			tvSummary = (TextView) findViewById(R.id.tvSummary);

			btnConnect = (Button) findViewById(R.id.btnConnect);
			btnConnect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mApp.authorize();					
				}
			});

		}
	}

	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

		@Override
		public void onSuccess() {
			tvSummary.setText("Connected as " + mApp.getUserName());
			btnConnect.setText("Disconnect");
		}

		@Override
		public void onFail(String error) {
			Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
		}
	};

	public class DownloadTask extends AsyncTask<URL, Integer, Long> {
		ArrayList<String> photoList;

		protected void onCancelled() {

		}

		protected void onPreExecute() {

		}

		protected Long doInBackground(URL... urls) {         
			long result = 0;

			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>(1);

				params.add(new BasicNameValuePair("count", "10"));


				InstagramRequest request = new InstagramRequest(mInstagramSession.getAccessToken());
				String response			 = request.createRequest("GET", "/tags/selfie/media/recent", params);
				if (!response.equals("")) {
					JSONObject jsonObj  = (JSONObject) new JSONTokener(response).nextValue();    				
					JSONArray jsonData	= jsonObj.getJSONArray("data");

					int length = jsonData.length();

					if (length > 0) {
						photoList = new ArrayList<String>();

						for (int i = 0; i < length; i++) {
							//JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("images").getJSONObject("low_resolution");
							JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("caption").getJSONObject("from");

							photoList.add(jsonPhoto.getString("profile_picture"));
						}
					}
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}

			return result;
		}

		protected void onProgressUpdate(Integer... progress) {              	
		}

		protected void onPostExecute(Long result) {
			mLoadingPb.setVisibility(View.GONE);

			if (photoList == null) {
				Toast.makeText(getApplicationContext(), "No Photos Available", Toast.LENGTH_LONG).show();
			} else {
				DisplayMetrics dm = new DisplayMetrics();

				getWindowManager().getDefaultDisplay().getMetrics(dm);

				int width 	= (int) Math.ceil((double) dm.widthPixels / 2);
				width=width-50;
				int height	= width;

				PhotoListAdapter adapter = new PhotoListAdapter(MainActivity.this);

				adapter.setData(photoList);
				adapter.setLayoutParam(width, height);

				mGridView.setAdapter(adapter);
			}
		}                

	}
}

