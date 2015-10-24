package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;

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
            ArrayList<String> resultList =  new ArrayList<String>();
            HttpURLConnection conn = null;
            StringBuilder responseJsonString = new StringBuilder();
            try{
                StringBuilder sb = new StringBuilder(Consts.API_SEARCH_STREAM_URL);
                sb.append("?keywords=" + URLEncoder.encode(mAutoCompleteTextView.getText().toString(), "utf8"));
                URL url = new URL(sb.toString());
                Log.w(TAG, "autocomplete url" + sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1){
                    responseJsonString.append(buff, 0, read);
                }
                Log.w(TAG,"response: "+responseJsonString.toString());
                try{
                    JSONObject jObject = new JSONObject(responseJsonString.toString());
                    JSONArray streamNames = jObject.getJSONArray("StreamNames");
                    JSONArray coverUrls = jObject.getJSONArray("CoverUrls");
                }catch(JSONException e){
                    Log.e(TAG, "JSON error " + e.toString());
                }
            }catch(MalformedURLException e){
                Log.e(TAG, "Error processing url", e);
            }catch(IOException e){
                Log.e(TAG, "Error connecting to Places API", e);
            }finally {
                if (conn != null){
                    conn.disconnect();
                }
            }
            // Handle return results and show messages/gridviews

        }
    };

    public void submitSearchKeywords(View view){
        Intent intent = new Intent(this, Homepage.class); //TODO: Add class to handle search or change this method
        EditText editText = (EditText) findViewById(R.id.search_text);
        String keywords = editText.getText().toString();
        intent.putExtra(SEARCH_KEYWORDS, keywords);
    }

}
