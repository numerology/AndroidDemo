package com.aptdemo.yzhao.androiddemo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewNearby extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
    private static final String TAG = "VeiwNearBy";
    private static final int IMAGE_PER_PAGE = Consts.NEARBY_PER_PAGE;

    Context context = this;

    private GridView mGridView;
    private LinearLayout mLinearLayout;
    private Button mPrePageButton;
    private Button mNextPageButton;
    private Button streamsButton;

    private LocationClient currentLocation;
    private String userEmail;

    String location;

    final private ArrayList<String> coverUrls = new ArrayList<String>();
    final private ArrayList<String> streamIds = new ArrayList<String>();
    final private ArrayList<String> pageCoverUrls = new ArrayList<String>();
    final private ArrayList<String> pageStreamIds = new ArrayList<String>();

    private ImageAdapter mImageAdapter = null;

    int currentPage = 0; // current page of results shown
    int totalPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_nearby);

        mGridView = (GridView) findViewById(R.id.nearby_gridview);
        mLinearLayout = (LinearLayout) findViewById(R.id.nearby_navigate);
        mPrePageButton = (Button) findViewById(R.id.nearby_pre_page);
        mPrePageButton.setOnClickListener(prePageHandler);
        mNextPageButton = (Button) findViewById(R.id.nearby_next_page);
        mNextPageButton.setOnClickListener(nextPageHandler);
        streamsButton = (Button) findViewById(R.id.nearby_all_streams);
        streamsButton.setOnClickListener(viewStreamsHandler);

        userEmail = getIntent().getStringExtra(Consts.USER_EMAIL_NAME);
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
        final String request_url = Consts.API_STREAM_NEARBY_URL;
        RequestParams params = new RequestParams();

        if(currentLocation != null) {
            location = currentLocation.getLastLocation().getLatitude() + "," + currentLocation.getLastLocation().getLongitude();
        }
        params.put("geolocation", location);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                coverUrls.clear();
                streamIds.clear();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jimgUrl = jObject.getJSONArray("image_url");
                    JSONArray jid = jObject.getJSONArray("stream_id");

                    for (int i = 0; i < jimgUrl.length(); i++) {
                        coverUrls.add(jimgUrl.getString(i));
                        streamIds.add(jid.getString(i));
                        //System.out.println("adding ID: " + jid.getString(i));
                    }
                    currentPage = 1;
                    totalPage = (int) Math.ceil(Double.valueOf(Integer.toString(coverUrls.size())) / Double.valueOf(Integer.toString(IMAGE_PER_PAGE)));
                    showStreamPage(currentPage);

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

    View.OnClickListener prePageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage > 1){
                currentPage = currentPage - 1;
                showStreamPage(currentPage);
            }
        }
    };
    View.OnClickListener nextPageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage < totalPage){
                currentPage = currentPage + 1;
                showStreamPage(currentPage);
            }
        }
    };
    View.OnClickListener viewStreamsHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent viewStreamsIntent = new Intent(context, ViewAllStream.class);
            viewStreamsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            viewStreamsIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
            startActivity(viewStreamsIntent);
            finish();
        }
    };

    private void showStreamPage(int page){
        if (totalPage == 0){
            Log.w(TAG, "total page is zero");
            pageCoverUrls.clear();
            mImageAdapter.notifyDataSetChanged();
            mGridView.invalidateViews();
            return;
        }
        if(page <= 0){
            Log.e(TAG, "show search result: page number too small");
            return;
        }
        if (page > totalPage){
            Log.w(TAG, "show search result: page number too large");
            return;
        }
        if (totalPage == 1){
            mLinearLayout.setVisibility(View.GONE);
        }else{
            mLinearLayout.setVisibility(View.VISIBLE);
            if (page < totalPage){
                mNextPageButton.setVisibility(View.VISIBLE);
            } else {
                mNextPageButton.setVisibility(View.INVISIBLE);
            }
            if (page == 1){
                mPrePageButton.setVisibility(View.INVISIBLE);
            }else{
                mPrePageButton.setVisibility(View.VISIBLE);
            }
        }
        pageStreamIds.clear();
        pageCoverUrls.clear();
        int startStreamNum = (page - 1)*IMAGE_PER_PAGE;
        int endStreamNum = (int) Math.min(page*IMAGE_PER_PAGE, coverUrls.size());
        for (int i = startStreamNum; i < endStreamNum ; i++) {
            pageCoverUrls.add(coverUrls.get(i));
            pageStreamIds.add(streamIds.get(i));
        }
        if (mImageAdapter == null){
            mImageAdapter = new ImageAdapter(context, pageCoverUrls);
            mGridView.setAdapter(mImageAdapter);
        }
        mImageAdapter.notifyDataSetChanged();
        Log.w(TAG, "# of images loaded is " + Integer.toString(pageCoverUrls.size()));
        //mGridView.setAdapter(mImageAdapter);
        mGridView.invalidateViews();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(context, "Checking single stream", Toast.LENGTH_SHORT).show();
                Intent viewStreamIntent = new Intent(context, ViewStreamActivity.class);
                viewStreamIntent.putExtra("stream_id", pageStreamIds.get(position));
                viewStreamIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
                startActivity(viewStreamIntent);
            }
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, ViewAllStream.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }

}
