package com.aptdemo.yzhao.mobile;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Yicong on 10/23/15.
 */
public class AutocompleteAPI {
    private static final String TAG = AutocompleteAPI.class.getSimpleName();

    Consts.AutocompleteType mAutocompleteType; //Used to decide the api to use: return stream names or  return all possible keywords
    String api_url = null;
    public AutocompleteAPI(Consts.AutocompleteType newType ){
        this.mAutocompleteType = newType;
        switch(newType){
            case ALL:   api_url = Consts.API_AUTOCOMPLETE_URL;
                         return;
            case STREAM_NAME:  api_url = Consts.API_STREAM_AUTOCOMPLETE_URL;
                                   return;
        }
    }

    public ArrayList<String> autocomplete(String input){
        ArrayList<String> resultList =  new ArrayList<String>();
        HttpURLConnection conn = null;
        StringBuilder responseJsonString = new StringBuilder();
        try{
            StringBuilder sb = new StringBuilder(api_url);
            sb.append("?keywords=" + URLEncoder.encode(input, "utf8"));
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
                JSONArray autocompleteList = new JSONArray(responseJsonString.toString());
                Log.w(TAG, "autocomplete array length" + Integer.toString(autocompleteList.length()));
                if (autocompleteList.length()>0) {
                    for (int i = 0; i < autocompleteList.length(); i++) {
                        resultList.add(autocompleteList.get(i).toString());
                    }
                }
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
        return resultList;
    }
}
