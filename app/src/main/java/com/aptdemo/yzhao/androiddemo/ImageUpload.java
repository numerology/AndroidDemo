package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class ImageUpload extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
    private static final String TAG = "Image Upload";
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int PRIVATE_TAKE_PHOTO = 3;
    private String userEmail;
    private String caption;

    Context context = this;
    private LocationClient currentLocation;
    private AutoCompleteTextView streamAutoCompleteTextView;
    String streamName;
    private Intent currentIntent;
    private ImageView mImageView;
    private Bitmap bitmapImage;

    private Button uploadButton;
    private Button chooseFromLibraryButton;
    private Button takePhotoBtn;
    private Button takePrivatePhotoBtn;
    private EditText captionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentIntent = getIntent();

        streamName = currentIntent.getStringExtra("stream_name");
        userEmail = currentIntent.getStringExtra(Consts.USER_EMAIL_NAME);
        Log.w(TAG, "stream name is" + streamName);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        captionText= (EditText) findViewById(R.id.captionTextArea);
        streamAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.stream_name);
        mImageView = (ImageView) findViewById(R.id.thumbnail);
        if (servicesConnected()) {
            System.out.println("servicesConnected");
            currentLocation = new LocationClient(this, this, this);
        }
        if (streamName != null && !streamName.isEmpty()) {
            streamAutoCompleteTextView.setVisibility(View.VISIBLE); //test passed 10/24 8:23pm
            streamAutoCompleteTextView.setText("Stream name: "+ streamName);
            streamAutoCompleteTextView.setKeyListener(null);
        } else {
            streamAutoCompleteTextView.setVisibility(View.VISIBLE);
            AutocompleteAdapter mAdapter = new AutocompleteAdapter(this, android.R.layout.simple_dropdown_item_1line);
            mAdapter.setAutocompleteAPIType(Consts.AutocompleteType.STREAM_NAME);
            streamAutoCompleteTextView.setAdapter(mAdapter);
        }
        // Choose image from library
        chooseFromLibraryButton = (Button) findViewById(R.id.choose_from_library);
        takePhotoBtn = (Button) findViewById(R.id.take_photo);
        takePrivatePhotoBtn = (Button) findViewById(R.id.private_take_photo);
        uploadButton = (Button) findViewById(R.id.upload_to_server);
        uploadButton.setVisibility(View.GONE);

        chooseFromLibraryButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // To do this, go to AndroidManifest.xml to add permission
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Start the Intent
                        startActivityForResult(galleryIntent, PICK_IMAGE);
                    }
                }
        );

        takePhotoBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager())!=null){
                            startActivityForResult(takePictureIntent, TAKE_PHOTO);
                        }
                    }
                }
        );
        takePrivatePhotoBtn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Intent takePrivatePhotoIntent = new Intent(context, TakePhotoActivity.class);
                        takePrivatePhotoIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
                        startActivityForResult(takePrivatePhotoIntent, PRIVATE_TAKE_PHOTO);
                    }
                }
        );


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

    @Override
    public void onConnected(Bundle b){
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        //String location=mLocationClient.getLastLocation().getLatitude()+"_"+mLocationClient.getLastLocation().getLongitude();
        //System.out.println(location);

    //    String streamName = getIntent().getStringExtra("streamName");
    //    String streamID = getIntent().getStringExtra("streamID");
        //TextView responseText = (TextView) this.findViewById(R.id.stream_name_upload);
        //responseText.setText(streamName);
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

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        currentLocation.disconnect();
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();

            // User had pick an image.

            String[] filePathColumn = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            // Link to the image

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imageFilePath = cursor.getString(columnIndex);
            cursor.close();

            // Bitmap imaged created and show thumbnail

            mImageView = (ImageView) findViewById(R.id.thumbnail);
            bitmapImage = BitmapFactory.decodeFile(imageFilePath);
            mImageView.setImageBitmap(bitmapImage);

            // Enable the upload button once image has been uploaded

            uploadButton.setVisibility(View.VISIBLE);
            uploadButton.setClickable(true);

            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();

                            if(currentLocation == null)
                                    System.out.println("Location Client Invalid");

                            String location = currentLocation.getLastLocation().getLatitude() + "," + currentLocation.getLastLocation().getLongitude();
                            System.out.println(location);
                            caption = captionText.getText().toString();
                            getUploadURL(b, streamName, location, caption);
                        }
                    }
            );
        }
        else if(requestCode == TAKE_PHOTO && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            mImageView = (ImageView) findViewById(R.id.thumbnail);
            bitmapImage = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(bitmapImage);
            uploadButton.setVisibility(View.VISIBLE);
            uploadButton.setClickable(true);
            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();

                            if (currentLocation == null)
                                System.out.println("Location Client Invalid");

                            String location = currentLocation.getLastLocation().getLatitude() + "," + currentLocation.getLastLocation().getLongitude();
                            System.out.println(location);
                            caption = captionText.getText().toString();
                            getUploadURL(b, streamName, location, caption);
                        }
                    }
            );

        } else if (requestCode == PRIVATE_TAKE_PHOTO && resultCode == RESULT_OK){
            Log.w(TAG, "result received for private take photo");
            Bundle extras = data.getExtras();
            ImageView mImageView = (ImageView) findViewById(R.id.thumbnail);
            byte[] array = (byte[]) extras.get("data");
            bitmapImage =  BitmapFactory.decodeByteArray(array, 0, array.length);
            mImageView.setImageBitmap(bitmapImage);
            uploadButton.setVisibility(View.VISIBLE);
            uploadButton.setClickable(true);
            uploadButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Get photo caption

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            byte[] b = baos.toByteArray();
                            byte[] encodedImage = Base64.encode(b, Base64.DEFAULT);
                            String encodedImageStr = encodedImage.toString();

                            if (currentLocation == null)
                                System.out.println("Location Client Invalid");

                            String location = currentLocation.getLastLocation().getLatitude() + "," + currentLocation.getLastLocation().getLongitude();
                            System.out.println(location);
                            caption = captionText.getText().toString();
                            getUploadURL(b, streamName, location, caption);
                        }
                    }
            );
        }
    }

    private void getUploadURL(final byte[] encodedImage, final String streamName, final String location, final String caption){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        String request_url="http://just-plate-107116.appspot.com/mobile/getUploadURL";
        System.out.println(request_url);
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            String upload_url;

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                try {
                    JSONObject jObject = new JSONObject(new String(response));

                    upload_url = jObject.getString("upload_url");
                    postToServer(encodedImage, streamName, upload_url, location, caption);

                } catch (JSONException j) {
                    System.out.println("JSON Error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Get_serving_url", "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    private void postToServer(byte[] encodedImage,String streamName, String upload_url, String location, String caption){
        System.out.println(upload_url);
        RequestParams params = new RequestParams();
        params.put("file",new ByteArrayInputStream(encodedImage));
        params.put("stream_name", streamName);
        params.put("geo_location", location);
        params.put("caption", caption);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(upload_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.w("async", "success!!!!");
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("Posting_to_blob", "There was a problem in posting : " + e.toString());
            }
        });
    }


    @Override
    public void onPause(){
        // recycle to save memory
        super.onPause();
        mImageView.setImageBitmap(null);
        if (bitmapImage != null){
            bitmapImage.recycle();
        }
    }
}
