package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchStream extends ActionBarActivity {//implements
        //GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{
    private final String TAG = "search-page";
    public final static String SEARCH_KEYWORDS = "com.aptdemo.yzhao.androiddemo.SEARCH_KEYWORDS";

    private AutoCompleteTextView mAutoCompleteTextView;
    private Button mSearchButton;
    private TextView mTextView;
    private GridView mGridView;

    final private ArrayList<String> streamNames = new ArrayList<String>();
    final private ArrayList<String> coverUrls = new ArrayList<String>();
    final private ArrayList<String> streamIds = new ArrayList<String>();
    private int numResult = 0;
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_stream);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.search_text);
        mAutoCompleteTextView.setAdapter(new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line));
        mSearchButton = (Button) findViewById(R.id.search_submit);
        mSearchButton.setOnClickListener(searchHandler);
        mTextView = (TextView) findViewById(R.id.search_info);
        mGridView = (GridView) findViewById(R.id.search_gridview);
    }

    View.OnClickListener searchHandler = new View.OnClickListener(){
        public void onClick(View v){
            if (mAutoCompleteTextView.getText().length() == 0){ // show hints if  text is empty
                Toast.makeText(context, "Keyword empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder responseJsonString = new StringBuilder();
            AsyncHttpClient httpClient = new AsyncHttpClient();

            try {
                StringBuilder sb = new StringBuilder(Consts.API_SEARCH_STREAM_URL);
                sb.append("?search_keywords=" + URLEncoder.encode(mAutoCompleteTextView.getText().toString(), "utf8"));
                Log.w(TAG, "search stream url: " + sb.toString());
                httpClient.get(sb.toString(), new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        streamNames.clear();
                        coverUrls.clear();
                        streamIds.clear();
                        numResult = 0;
                        try {
                            JSONObject jObject = new JSONObject(new String(responseBody));
                            JSONArray jsonStreamNames = jObject.getJSONArray("StreamNames");
                            JSONArray jsonCoverUrls = jObject.getJSONArray("CoverUrls");
                            JSONArray jsonStreamIds = jObject.getJSONArray("StreamIds");
                            if (jsonStreamNames.length() > 0) {
                                numResult = jsonStreamNames.length();
                                for (int i = 0; i < jsonStreamNames.length(); i++) {
                                    streamNames.add(jsonStreamNames.get(i).toString());
                                    coverUrls.add(jsonCoverUrls.get(i).toString());
                                    streamIds.add(jsonStreamIds.get(i).toString());
                                }
                                StringBuilder searchInfoSb = new StringBuilder();
                                searchInfoSb.append(Integer.toString(numResult) + " results found for");
                                searchInfoSb.append(mAutoCompleteTextView.getText().toString());
                                mTextView.setText(searchInfoSb.toString());
                                mGridView.setAdapter(new ImageAdapter(context, coverUrls)); // TODO: set new adapter or change the data of the original adapter ? notifyDataSetChanged()?
                                mGridView.invalidateViews();
                                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intentViewStream = new Intent(context, ViewStreamActivity.class);
                                        intentViewStream.putExtra("stream_id", streamIds.get(position));
                                        startActivity(intentViewStream);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "json exception: " + e.toString());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG, "fail to get response from server " + error.toString());
                    }
                });
            }catch (IOException e) { // catch exception in url encode
                Log.e(TAG, "fail to encode url for searching" + e.toString());
            }
        }
    };

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
