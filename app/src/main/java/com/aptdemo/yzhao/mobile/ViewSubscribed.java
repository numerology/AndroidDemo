package com.aptdemo.yzhao.mobile;

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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewSubscribed extends ActionBarActivity {


    private String TAG = "ListStreams";
    Context context = this;
  //  private final String userEmail = getIntent().getStringExtra("user_email");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_subscribed);
        final String userEmail = getIntent().getStringExtra("user_email");
        final String request_url = Consts.API_STREAM_SUBSCRIBED_URL;
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("user_email",userEmail);
        httpClient.get(request_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response){
                final ArrayList<String> coverURLs = new ArrayList<String>();
                final ArrayList<String> streamIDs = new ArrayList<String>();

                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jcoverUrl = jObject.getJSONArray("cover_url");
                    JSONArray jid = jObject.getJSONArray("streams_id");

                    for (int i = 0; i < jcoverUrl.length(); i++){
                        coverURLs.add(jcoverUrl.getString(i));
                        streamIDs.add(jid.getString(i));
                        System.out.println("adding ID: "+jid.getString(i));
                    }

                    GridView gridview = (GridView) findViewById(R.id.SubscribedStreamGrid);
                    gridview.setAdapter(new ImageAdapter(context,coverURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                            Toast.makeText(context, "Checking single stream", Toast.LENGTH_SHORT).show();
                            Intent viewStreamIntent = new Intent(context, ViewStreamActivity.class);
                            viewStreamIntent.putExtra("stream_id",streamIDs.get(position));
                            viewStreamIntent.putExtra("user_email",userEmail);
                            startActivity(viewStreamIntent);
                        }
                    });

                }
                catch(JSONException j){
                    System.out.println("JSON Error in List Stream");
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e){
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
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
