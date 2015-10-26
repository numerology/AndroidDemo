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
import android.widget.LinearLayout;
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
    Context context = this;

    private AutoCompleteTextView mAutoCompleteTextView;
    private Button mSearchButton;
    private LinearLayout mLinearLayout;
    private Button mPrePageButton;
    private Button mNextPageButton;
    private TextView mTextView;
    private GridView mGridView;

    final private ArrayList<String> streamNames = new ArrayList<String>();
    final private ArrayList<String> coverUrls = new ArrayList<String>();
    final private ArrayList<String> streamIds = new ArrayList<String>();
    final private ArrayList<String> pageStreamNames = new ArrayList<String>();
    final private ArrayList<String> pageCoverUrls = new ArrayList<String>();
    final private ArrayList<String> pageStreamIds = new ArrayList<String>();

    int currentPage = 0; // current page of results shown
    private int totalPage = 0;
    private int numResult = 0;
    private String keywords;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_stream);
        Intent intent = getIntent();
        keywords = intent.getStringExtra(Consts.KEYWORD_NAME);
        userEmail = intent.getStringExtra("user_email");

        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.search_text);
        mAutoCompleteTextView.setAdapter(new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line));
        mSearchButton = (Button) findViewById(R.id.search_submit);
        mSearchButton.setOnClickListener(searchHandler);
        mTextView = (TextView) findViewById(R.id.search_info);
        mGridView = (GridView) findViewById(R.id.search_gridview);
        mLinearLayout = (LinearLayout) findViewById(R.id.search_navigate);
        mPrePageButton = (Button) findViewById(R.id.search_pre_page);
        mPrePageButton.setOnClickListener(prePageHandler);
        mNextPageButton = (Button) findViewById(R.id.search_next_page);
        mNextPageButton.setOnClickListener(nextPageHandler);
        searchKeyword();
    }

    View.OnClickListener searchHandler = new View.OnClickListener(){
        public void onClick(View v){
            keywords = mAutoCompleteTextView.getText().toString();
            searchKeyword();
        }
    };

    private void searchKeyword(){
        if (keywords ==null || keywords.length() == 0 || keywords.isEmpty()){ // show hints if  text is empty
            Toast.makeText(context, Consts.SEARCH_KEYWORD_EMPTY_WARNING, Toast.LENGTH_SHORT).show();
            return;
        }
        AsyncHttpClient httpClient = new AsyncHttpClient();
        try {
            StringBuilder sb = new StringBuilder(Consts.API_SEARCH_STREAM_URL);
            sb.append("?search_keywords=" + URLEncoder.encode(keywords, "utf8"));
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
                        if (jsonStreamNames.length() >= 0) {
                            numResult = jsonStreamNames.length();
                            for (int i = 0; i < jsonStreamNames.length(); i++) {
                                streamNames.add(jsonStreamNames.get(i).toString());
                                coverUrls.add(jsonCoverUrls.get(i).toString());
                                streamIds.add(jsonStreamIds.get(i).toString());
                            }
                            StringBuilder searchInfoSb = new StringBuilder();
                            searchInfoSb.append(Integer.toString(numResult) + " results found for ");
                            searchInfoSb.append(keywords);
                            mTextView.setText(searchInfoSb.toString());
                            currentPage = 1;
                            totalPage = (int) Math.ceil(Double.valueOf(Integer.toString(streamNames.size()))/Double.valueOf(Integer.toString(Consts.SEARCH_STREAM_PER_PAGE)));
                            Log.w(TAG, "total page: " + Integer.toString(totalPage));
                            showSearchResult(currentPage);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, ViewAllStream.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // TODO: check whether this works, if works, apply to all activities
            startActivity(intent);
            finish();
            return true;
        }
        return true;
    }

    private void showSearchResult(int page){
        if (totalPage == 0){
            Log.w(TAG, "total page is zero");
            mGridView.setAdapter(new ImageAdapter(context, coverUrls));
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
        pageStreamNames.clear();
        pageCoverUrls.clear();
        pageStreamIds.clear();
        int startStreamNum = (page - 1)*Consts.SEARCH_STREAM_PER_PAGE;
        int endStreamNum = (int) Math.min(page*Consts.SEARCH_STREAM_PER_PAGE, coverUrls.size());
        for (int i = startStreamNum; i < endStreamNum ; i++){
            pageStreamNames.add(streamNames.get(i));
            pageCoverUrls.add(coverUrls.get(i));
            pageStreamIds.add(streamIds.get(i));
        }
        Log.w(TAG, "# of images loaded is "+Integer.toString(pageCoverUrls.size()));
        mGridView.setAdapter(new ImageAdapter(context, pageCoverUrls));
        mGridView.invalidateViews();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentViewStream = new Intent(context, ViewStreamActivity.class);
                intentViewStream.putExtra("stream_id", pageStreamIds.get(position));
                startActivity(intentViewStream);
            }
        });
    }
    View.OnClickListener prePageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage > 1){
                currentPage = currentPage - 1;
                showSearchResult(currentPage);
            }
        }
    };
    View.OnClickListener nextPageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage < totalPage){
                currentPage = currentPage + 1;
                showSearchResult(currentPage);
            }
        }
    };
}
