package com.aptdemo.yzhao.androiddemo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.content.Context;
import android.widget.ImageView;
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
    Context context = this;
    String streamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);
        Intent currentIntent = getIntent();
        String streamID = currentIntent.getStringExtra("stream_id");
        final String request_url = Consts.API_STREAM_VIEW_URL;
        RequestParams params = new RequestParams();
        params.put("stream_id",streamID);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, params, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){

                final ArrayList<String> imageURLs = new ArrayList<String>();
                try{
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray imgUrl = jObject.getJSONArray("image_url");
                    streamName = jObject.getString("stream_name");

                    for (int i = 0; i < imgUrl.length(); i++){
                        imageURLs.add(imgUrl.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.singleStreamGrid);
                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();

                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.thumbnail);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);

                            Picasso.with(context).load(imageURLs.get(position)).into(image);

                            imageDialog.show();
                        }
                    });

                }
                catch(JSONException j){
                    System.out.println("JSON Error in View Stream");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e){
                Log.e(TAG, "There was a problem in viewing a stream : " + e.toString());
            }

        });

    }

    public void uploadImage(View view){
        Intent intent= new Intent(this, ImageUpload.class);
        intent.putExtra("stream_name", streamName);
        startActivity(intent);
    }
    @Override
    public void onResume(){
        super.onResume();
        setContentView(R.layout.activity_view_stream);
        Intent currentIntent = getIntent();
        String streamID = currentIntent.getStringExtra("stream_id");
        final String request_url = Consts.API_STREAM_VIEW_URL;
        RequestParams params = new RequestParams();
        params.put("stream_id",streamID);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                final ArrayList<String> imageURLs = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray imgUrl = jObject.getJSONArray("image_url");
                    streamName = jObject.getString("stream_name");

                    for (int i = 0; i < imgUrl.length(); i++) {
                        imageURLs.add(imgUrl.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.singleStreamGrid);
                    gridview.setAdapter(new ImageAdapter(context, imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();

                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.thumbnail);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);

                            Picasso.with(context).load(imageURLs.get(position)).into(image);

                            imageDialog.show();
                        }
                    });

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
