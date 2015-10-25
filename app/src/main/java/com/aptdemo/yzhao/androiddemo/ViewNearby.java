package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewNearby extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
    private String TAG = "VeiwNearBy";
    Context context = this;
    private LocationClient currentLocation;
    String location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearby);
        if (servicesConnected()) {
            System.out.println("servicesConnected");
            currentLocation = new LocationClient(this, this, this);
        }

    }

    public void onConnected(Bundle b){
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        //String location=mLocationClient.getLastLocation().getLatitude()+"_"+mLocationClient.getLastLocation().getLongitude();
        //System.out.println(location);

        //    String streamName = getIntent().getStringExtra("streamName");
        //    String streamID = getIntent().getStringExtra("streamID");
        //TextView responseText = (TextView) this.findViewById(R.id.stream_name_upload);
        //responseText.setText(streamName);
        final String request_url = "http://just-plate-107116.appspot.com/mobile/stream_nearby";

        RequestParams params = new RequestParams();

        if(currentLocation != null) {
            location = currentLocation.getLastLocation().getLatitude() + "," + currentLocation.getLastLocation().getLongitude();
        }
        params.put("geolocation", location);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> imgURLs = new ArrayList<String>();
                final ArrayList<String> streamIDs = new ArrayList<String>();

                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jimgUrl = jObject.getJSONArray("image_url");
                    JSONArray jid = jObject.getJSONArray("stream_id");

                    for (int i = 0; i < jimgUrl.length(); i++) {
                        imgURLs.add(jimgUrl.getString(i));
                        streamIDs.add(jid.getString(i));
                        System.out.println("adding ID: " + jid.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.NearbyStreamGrid);
                    gridview.setAdapter(new ImageAdapter(context, imgURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            Toast.makeText(context, "Checking single stream", Toast.LENGTH_SHORT).show();
                            Intent viewStreamIntent = new Intent(context, ViewStreamActivity.class);
                            viewStreamIntent.putExtra("stream_id", streamIDs.get(position));
                            startActivity(viewStreamIntent);
                        }
                    });

                } catch (JSONException j) {
                    System.out.println("JSON Error in Stream Nearby");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }
    @Override
    public void onConnectionFailed(ConnectionResult r){

    }
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        currentLocation.connect();

    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            System.out.println("CONNECTION FAILED");

            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, Homepage.class);
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }

}
