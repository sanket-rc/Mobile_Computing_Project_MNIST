package com.android.mobile_application;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImageDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Initialize variables
    ImageView capturedImage;
    Spinner spinner;
    Button btnUploadImg;
    Bitmap bitmap;
    EditText ipAddress;
    EditText portNumber;
    TextView responseText;
    private final String PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
    private final String TAG = "MainActivity";

    DigitClassifier digitClassifier = new DigitClassifier(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_data);

        //spinner = findViewById(R.id.directory);
        capturedImage = findViewById(R.id.image_view2);
        btnUploadImg = findViewById(R.id.btn_upload_img);
        ipAddress = findViewById(R.id.ip_address);
        portNumber = findViewById(R.id.port_number);
        responseText = findViewById(R.id.responseText);

        Bundle bundle = getIntent().getExtras();

        // Retrieve the captured image
        if(bundle != null){
            Intent intent = getIntent();
            bitmap = (Bitmap) intent.getParcelableExtra("bitmapImage");
            capturedImage.setImageBitmap(bitmap);
        }

        // Listens for the upload button to get clicked
        btnUploadImg.setOnClickListener((view) -> classifyDrawing());

        // Set up digit classifier
        try {
            digitClassifier.initialize();
            Log.e(TAG, "Setting up digit classifier.");
        }
        catch (Exception e){
            Log.e(TAG, "Error to setting up digit classifier.");
        }


    }

    private void classifyDrawing() {
        Bitmap lBitmap = bitmap;
        if (lBitmap != null)
        {
            Log.e(TAG, "Bitmap Here.");
        }
        if (digitClassifier.isInitialized())
        {
            Log.e(TAG, "Initialized Here.");
        }



        if ((lBitmap != null) && (digitClassifier.isInitialized())) {
            try{
                responseText.setText(digitClassifier.classify(lBitmap));
                Log.e(TAG, "Classifying drawing.");
            }
            catch (Exception e) {
                responseText.setText(e.toString());
                Log.e(TAG, "Error classifying drawing.", e);
            }
        }
    }

    public void uploadImageToServer(){
        String port = portNumber.getText().toString();
        boolean isNumber = port.matches("[0-9]+");
        String address = ipAddress.getText().toString();

        if(TextUtils.isEmpty(ipAddress.getText()) || !Pattern.matches(PATTERN, address)){
            ipAddress.setError("Error in Ip Address");
            ipAddress.requestFocus();
        }
        else if(TextUtils.isEmpty(portNumber.getText()) || !isNumber || Integer.parseInt(port) > 65536) {
            portNumber.setError("Error in port number");
            portNumber.requestFocus();
        }else{
            makeResponseBody();
        }
    }

    public void makeResponseBody(){
        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        BitmapFactory.Options factoryOptions = new BitmapFactory.Options();

        // Each pixel is stored on 2 bytes and only the RGB channels are encoded: red is stored with 5 bits of precision (32 possible values),
        // green is stored with 6 bits of precision (64 possible values) and blue is stored with 5 bits of precision.
        factoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, arrayStream);
        byte[] byteArray = arrayStream.toByteArray();

        Long timeStampLong = System.currentTimeMillis()/1000;
        String timeStamp = timeStampLong.toString();

        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", timeStamp + ".jpg", RequestBody.create(byteArray, MediaType.parse("image/*jpg")))
                .build();

        String postUrl= "http://" + ipAddress.getText().toString() + ":" + portNumber.getText().toString() +"/uploadImage";
        // String postUrl= "http://192.168.0.101:5001/uploadImage";
        postRequest(postUrl, postBody);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // To access the TextView inside the UI-thread, the code is added inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            responseText.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Cancel the request on failure.
                call.cancel();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        responseText.setText("Failed to Connect to Server");
                    }
                });
            }

        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(getApplicationContext(), choice, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onDestroy() {
        digitClassifier.close();
        super.onDestroy();
    }
}