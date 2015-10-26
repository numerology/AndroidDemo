package com.aptdemo.yzhao.mobile;

import android.app.Dialog;
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
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewStreamActivity extends ActionBarActivity {
    private String TAG = "View Stream";
    private final int IMAGE_PER_PAGE = Consts.VIEW_A_STREAM_PER_PAGE;
    Context context = this;
    private boolean ownerflag;
    private Button uploadBtn;
    private TextView streamNameTextView;
    private GridView mGridView;
    private LinearLayout mLinearLayout;
    private Button mPrePageButton;
    private Button mNextPageButton;
    private Button streamsButton;

    String streamName;
    private String userEmail;
    private String streamID;
    ArrayList<String> imageURLs = new ArrayList<String>();
    ArrayList<String> pageImageURLs = new ArrayList<String>();
    ImageAdapter mImageAdapter = null;

    int currentPage = 0; // current page of results shown
    int totalPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);

        uploadBtn = (Button) findViewById(R.id.open_image_upload_page);
        streamNameTextView = (TextView) findViewById(R.id.view_a_stream_stream_name);
        mGridView = (GridView) findViewById(R.id.view_a_stream_gridview);
        mLinearLayout = (LinearLayout) findViewById(R.id.view_a_stream_navigate);
        mPrePageButton = (Button) findViewById(R.id.view_a_stream_pre_page);
        mPrePageButton.setOnClickListener(prePageHandler);
        mNextPageButton = (Button) findViewById(R.id.view_a_stream_next_page);
        mNextPageButton.setOnClickListener(nextPageHandler);
        streamsButton = (Button) findViewById(R.id.view_a_stream_all_streams);
        streamsButton.setOnClickListener(viewStreamsHandler);

        Intent currentIntent = getIntent();
        userEmail = currentIntent.getStringExtra(Consts.USER_EMAIL_NAME);
        Log.d(TAG, "email is: "+userEmail.toString());
        streamID = currentIntent.getStringExtra("stream_id");

        System.out.println("on creating view stream");
    }

    public void uploadImage(View view){
        Intent intent= new Intent(this, ImageUpload.class);
        intent.putExtra("stream_name", streamName);
        intent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
        startActivity(intent);
    }
    @Override
    public void onResume(){


        super.onResume();
        //uploadBtn.setVisibility(View.GONE);


        final String request_url = Consts.API_STREAM_VIEW_URL;
        RequestParams params = new RequestParams();
        params.put("stream_id",streamID);
        params.put("user_email", userEmail);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                imageURLs.clear(); // forgot to clear 10/26/2015
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray imgUrl = jObject.getJSONArray("image_url");
                    Log.d(TAG, "received image number is: " + Integer.toString(imgUrl.length()));
                    streamName = jObject.getString("stream_name");
                    setStreamName();
                    ownerflag = jObject.getBoolean("owner_flag");
                    Log.d(TAG, "ownerflag= " + ownerflag);

                    uploadBtn.setEnabled(ownerflag);
                    if (ownerflag) {
                        uploadBtn.setVisibility(View.VISIBLE);
                        uploadBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                uploadImage(view);
                            }
                        });
                    }


                    for (int i = 0; i < imgUrl.length(); i++) {
                        imageURLs.add(imgUrl.getString(i));
                    }

                    currentPage = 1;
                    totalPage = (int) Math.ceil(Double.valueOf(Integer.toString(imageURLs.size())) / Double.valueOf(Integer.toString(IMAGE_PER_PAGE)));
                    showImagePage(currentPage);

                } catch (JSONException j) {
                    System.out.println("JSON Error in View Stream");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in viewing a stream : " + e.toString());
            }
        });

    }

    private void showImagePage(int page){
        if (totalPage == 0){
            Log.w(TAG, "total page is zero");
            pageImageURLs.clear();
            if (mImageAdapter != null){
                mImageAdapter.notifyDataSetChanged();
            }
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
        pageImageURLs.clear();
        int startStreamNum = (page - 1)*IMAGE_PER_PAGE;
        int endStreamNum = (int) Math.min(page*IMAGE_PER_PAGE, imageURLs.size());
        for (int i = startStreamNum; i < endStreamNum ; i++){
            pageImageURLs.add(imageURLs.get(i));
        }
        if (mImageAdapter == null){
            mImageAdapter = new ImageAdapter(context, pageImageURLs);
        }else{
            mImageAdapter.notifyDataSetChanged();
        }
        Log.w(TAG, "# of images loaded is " + Integer.toString(pageImageURLs.size()));
        mGridView.setAdapter(mImageAdapter);
        mGridView.invalidateViews();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
               // Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                Dialog imageDialog = new Dialog(context);
                imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                imageDialog.setContentView(R.layout.thumbnail);
                ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);
                Picasso.with(context).load(pageImageURLs.get(position)).into(image);
                imageDialog.show();
            }
        });
    }

    View.OnClickListener prePageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage > 1){
                currentPage = currentPage - 1;
                showImagePage(currentPage);
            }
        }
    };
    View.OnClickListener nextPageHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPage < totalPage){
                currentPage = currentPage + 1;
                showImagePage(currentPage);
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
    public void setStreamName(){
        if (streamName != null && !streamName.isEmpty()){
            StringBuilder sb = new StringBuilder("View a Stream ");
            sb.append(streamName);
            streamNameTextView.setText(sb.toString());
        } else {
            streamNameTextView.setText("Fail to find stream name");
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
