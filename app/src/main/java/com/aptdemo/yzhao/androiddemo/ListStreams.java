package com.aptdemo.yzhao.androiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListStreams extends ActionBarActivity {
    private String TAG = "ListStreams";
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_streams);

        final String request_url = "http://just-plate-107116.appspot.com/mobile/stream_list";
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
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

                    GridView gridview = (GridView) findViewById(R.id.streamGrid);
                    gridview.setAdapter(new ImageAdapter(context,coverURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                            Toast.makeText(context, "Checking single stream", Toast.LENGTH_SHORT).show();
                            Intent viewStreamIntent = new Intent(context, ViewStreamActivity.class);
                            viewStreamIntent.putExtra("stream_id",streamIDs.get(position));
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

}
