package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewAllStream extends ActionBarActivity {
    private static final String TAG = "View all streams";
    private enum SubscribeStream{
        ALL,
        SUBSCRIBE_STREAM
    }
    Context context = this;
    GridView mGridView;
    AutoCompleteTextView mSearchText;
    Button searchBtn;
    Button nearbyBtn;
    Button showSubscribeBtn;
    ArrayList<String> coverURLs = new ArrayList<String>();
    ArrayList<String> streamIDs = new ArrayList<String>();
    ImageAdapter mImageAdapter = null;
    private String userEmail;
    private SubscribeStream showSubscribeState = SubscribeStream.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_stream);
        userEmail = getIntent().getStringExtra(Consts.USER_EMAIL_NAME);
        mGridView = (GridView) findViewById(R.id.view_all_streams_gridview);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.view_all_streams_search_text);
        mSearchText.setAdapter(new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line));
        searchBtn = (Button) findViewById(R.id.view_all_streams_search_submit);
        searchBtn.setOnClickListener(searchHandler);
        nearbyBtn = (Button) findViewById(R.id.view_all_streams_nearby);
        nearbyBtn.setOnClickListener(nearbyHandler);
        showSubscribeBtn = (Button) findViewById(R.id.view_all_streams_subscribe);
        showSubscribeBtn.setOnClickListener(showSubscribeHandler);
        showSubscribeBtn.setText(Consts.SUBSCRIBE_BUTTON_SHOW_SUBSCRIBE);
        checkLoggedIn();
    }
    @Override
    public void onStart(){ // TODO: current plan is to check loggin state in onStart, should be deleted
        super.onStart();
        Log.d(TAG, "started, loggedin state is " + Boolean.toString(isLoggedIn()));
    }
    @Override
    public void onResume(){ //  get the streams back
        super.onResume();
        Log.d(TAG, "userEmail: " + userEmail);
        refreshGridView();
    }

    View.OnClickListener searchHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            String keywords = mSearchText.getText().toString();
            if (keywords == null || keywords.isEmpty() || keywords.length() == 0){
                Toast.makeText(context, Consts.SEARCH_KEYWORD_EMPTY_WARNING, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent searchIntent = new Intent(context, SearchStream.class);
            searchIntent.putExtra(Consts.KEYWORD_NAME, keywords);
            searchIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
            startActivity(searchIntent);
        }
    };

    View.OnClickListener nearbyHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent nearbyIntent = new Intent(context, ViewNearby.class);
            nearbyIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
            startActivity(nearbyIntent);
        }
    };
    View.OnClickListener showSubscribeHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkLoggedIn();
            if (!isLoggedIn()){
                return;
            }
            if (showSubscribeState == SubscribeStream.ALL){
                showSubscribeState = SubscribeStream.SUBSCRIBE_STREAM;
                refreshGridView();
                showSubscribeBtn.setText(Consts.SUBSCRIBE_BUTTON_SHOW_ALL);
            } else {
                showSubscribeState = SubscribeStream.ALL;
                refreshGridView();
                showSubscribeBtn.setText(Consts.SUBSCRIBE_BUTTON_SHOW_SUBSCRIBE);
            }
        }
    };

    private void refreshGridView(){ // update streams according to showSubscribeState
        Log.d(TAG, "refreshGridView runs");
        String request_url = Consts.API_STREAM_LIST_URL;
        if (showSubscribeState == SubscribeStream.SUBSCRIBE_STREAM){
            request_url = Consts.API_STREAM_SUBSCRIBED_URL; //TODO: send max number of stream to return ?
        }
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d(TAG, "get data from server");
                if (mImageAdapter != null){
                    coverURLs.clear();
                    streamIDs.clear();
                    mImageAdapter.notifyDataSetInvalidated();
                }
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jcoverUrl = jObject.getJSONArray("cover_url");
                    JSONArray jid = jObject.getJSONArray("streams_id");
                    Log.d(TAG, "jcoverUrl length: " + Integer.toString(jcoverUrl.length()));
                    for (int i = 0; i < Math.min(jcoverUrl.length(), Consts.VIEW_ALL_STREAM_PER_PAGE); i++){
                        if(jcoverUrl.getString(i).length()>1) {
                            coverURLs.add(jcoverUrl.getString(i));
                        }else
                        {
                            coverURLs.add(Consts.DEFAULT_COVER_URL);
                        }
                        streamIDs.add(jid.getString(i));
                        //System.out.println("adding ID: " + jid.getString(i));
                        System.out.println("adding url: "+jcoverUrl.getString(i));
                    }
                    if (mImageAdapter == null){
                        mImageAdapter = new ImageAdapter(context, coverURLs);
                        mGridView.setAdapter(mImageAdapter);
                    }else{
                        mImageAdapter.notifyDataSetChanged();
                    }
                    mGridView.invalidateViews();
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                            Toast.makeText(context, "Checking single stream", Toast.LENGTH_SHORT).show();
                            Intent viewStreamIntent = new Intent(context, ViewStreamActivity.class);
                            viewStreamIntent.putExtra("stream_id",streamIDs.get(position));
                            viewStreamIntent.putExtra(Consts.USER_EMAIL_NAME,userEmail);
                            startActivity(viewStreamIntent);
                        }
                    });

                }
                catch(JSONException j){
                    System.out.println("JSON Error in List Stream");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "There was a problem in retrieving the url : " + error.toString());
            }
        });

    }

    private boolean isLoggedIn(){ // TODO: can modify this function to check login status
        if (userEmail != null && !userEmail.isEmpty() && userEmail.length()>0){
            return true;
        }else{
            return false;
        }
    }
    private void checkLoggedIn(){
        if (!isLoggedIn()){
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT);
            showSubscribeBtn.setEnabled(false);
            showSubscribeBtn.setText(Consts.SUBSCRIBE_BUTTON_SHOW_SUBSCRIBE);
            showSubscribeState = SubscribeStream.ALL; // YW: 10/26/2015: should view all instread of subscribe stream !!
        }
    }
}
