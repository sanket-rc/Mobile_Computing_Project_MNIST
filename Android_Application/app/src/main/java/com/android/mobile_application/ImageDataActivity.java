package com.android.mobile_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
<<<<<<< Updated upstream
import android.graphics.drawable.BitmapDrawable;
=======
import android.graphics.Color;
>>>>>>> Stashed changes
import android.os.Bundle;
import android.text.TextUtils;
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
<<<<<<< Updated upstream
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
=======
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

>>>>>>> Stashed changes

public class ImageDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Initialize variables
    ImageView capturedImage;
    Spinner spinner;
    Button btnUploadImg;
    Bitmap bitmap;
<<<<<<< Updated upstream
    EditText ipAddress;
    EditText portNumber;
    private final String PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
=======

    TextView responseText;
//    TextView responseText1;
//    TextView responseText2;
//    TextView responseText3;
    Gateway gateway;
    HashMap<String, BluetoothSocket> map;
    List<String> accepteddevices;

    private final String PATTERN = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
    private final String TAG = "MainActivity";

    DigitClassifier digitClassifier0 = new DigitClassifier(this);
    DigitClassifier digitClassifier1 = new DigitClassifier(this);
    DigitClassifier digitClassifier2 = new DigitClassifier(this);
    DigitClassifier digitClassifier3 = new DigitClassifier(this);
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_data);

        //spinner = findViewById(R.id.directory);
        capturedImage = findViewById(R.id.image_view2);
        btnUploadImg = findViewById(R.id.btn_upload_img);
<<<<<<< Updated upstream
        ipAddress = findViewById(R.id.ip_address);
        portNumber = findViewById(R.id.port_number);
=======

        responseText = findViewById(R.id.responseText);
//        responseText1 = findViewById(R.id.responseText1);
//        responseText2 = findViewById(R.id.responseText2);
//        responseText3 = findViewById(R.id.responseText3);
        map=Master.map;
        accepteddevices=Master.accepteddevices;
>>>>>>> Stashed changes

        Bundle bundle = getIntent().getExtras();

        // Retrieve the captured image
        if(bundle != null){
            Intent intent = getIntent();
            bitmap = (Bitmap) intent.getParcelableExtra("bitmapImage");
            capturedImage.setImageBitmap(bitmap);
        }

        // Listens for the upload button to get clicked
<<<<<<< Updated upstream
        btnUploadImg.setOnClickListener((view) -> uploadImageToServer());
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
=======
        btnUploadImg.setOnClickListener((view) -> classifyDrawing(bitmap));

        // Set up digit classifier
        try {
            digitClassifier0.initialize(0);
            digitClassifier1.initialize(1);
            digitClassifier2.initialize(2);
            digitClassifier3.initialize(3);
            Log.e(TAG, "Setting up digit classifier.");
        }
        catch (Exception e){
            Log.e(TAG, "Error to setting up digit classifier.");
        }


    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedByte;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    private void classifyDrawing(Bitmap bitmap) {

        if (bitmap != null) {
            Log.e(TAG, "Bitmap Here.");
        }
//        if (digitClassifier.isInitialized()) {
//            Log.e(TAG, "Initialized Here.");
//        }
        double[] arr = new double[10];
//        arr[8] = 100;
        if ((bitmap != null)) {
            DigitClassifier digitClassifier = null;
            double max = 0.0;
            int index = 0;
            for(int i = 0; i < 4; i++){ // Make it 4
                switch(i){
                    case 0:
                        digitClassifier = digitClassifier0;
                        break;
                    case 1:
                        digitClassifier = digitClassifier1;
                        break;
                    case 2:
                        digitClassifier = digitClassifier2;
                        break;
                    case 3:
                        digitClassifier = digitClassifier3;
                        break;
                }

                try {
                    Bitmap bMap = preprocessImage(bitmap, i);
                    String bitmap_str = BitMapToString(bMap);
                    responseText.setText(digitClassifier.classify(bMap).toString());
                    float[] res = digitClassifier.classify(bMap);
                    double[] doubleArr = new double[10];

                    for(int j=0; j<10; j++){
                        doubleArr[j] = (double)res[j];
                        arr[j] = arr[j] + doubleArr[j] ;
                    }

//                    maxValues[i] = maxIndex;
//                String bitmap_str = BitMapToString(bitmap);
//                Bitmap deccoded_bm = StringToBitMap(bitmap_str);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("classify_image",i);
                        jsonObject.put("data",1);
                        jsonObject.put("quadrant", i);
                        jsonObject.put("mat_img", Arrays.toString(doubleArr));
//                    jsonObject.put("matrix2", new JSONArray(matrixint2));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonString = jsonObject.toString();
                    // System.out.println(i+"->"+jsonString);


                    gateway = new Gateway(map.get(accepteddevices.get(i)), Master.handler);
                    gateway.write(jsonString.getBytes());

                    //gateway.write(BitMapToString(bitmap));
                    //responseText.setText(digitClassifier.classify(preprocessImage(bitmap, 0)));
                    Log.e(TAG, "Classifying TL.");
                } catch (Exception e) {
                    responseText.setText(e.toString());
                    Log.e(TAG, "Error classifying TL.", e);
                }
            }
            for(int j=0; j<10; j++){
                if(arr[j] > max){
                    max = arr[j];
                    index = j;
                }
            }
            responseText.setText("Classification result: " + index);


//            try {
//                String bitmap_str = BitMapToString(preprocessImage(bitmap, 1));
//                responseText1.setText(digitClassifier.classify(preprocessImage(bitmap, 1)));
//                Log.e(TAG, "Classifying TR.");
//            } catch (Exception e) {
//                responseText1.setText(e.toString());
//                Log.e(TAG, "Error classifying TR.", e);
//            }
//
//            try {
//                responseText2.setText(digitClassifier.classify(preprocessImage(bitmap, 2)));
//                Log.e(TAG, "Classifying BL.");
//            } catch (Exception e) {
//                responseText2.setText(e.toString());
//                Log.e(TAG, "Error classifying BL.", e);
//            }
//            try {
//                responseText3.setText(digitClassifier.classify(preprocessImage(bitmap, 3)));
//                Log.e(TAG, "Classifying BR.");
//            } catch (Exception e) {
//                responseText3.setText(e.toString());
//                Log.e(TAG, "Error classifying BR.", e);
//            }
        }
    }

    private Bitmap preprocessImage(Bitmap bitmap, int version) {
        int width = bitmap.getWidth()/2;
        int height = bitmap.getHeight()/2;
//        Bitmap bnw = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Bitmap bnw = bitmap.copy(bitmap.getConfig(),true);

        int R, G, B;
        int pixel;
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                // get pixel color
                pixel = bitmap.getPixel(x, y);
>>>>>>> Stashed changes

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
                        TextView responseText = findViewById(R.id.responseText);
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
                        TextView responseText = findViewById(R.id.responseText);
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
<<<<<<< Updated upstream
=======

    @Override
    public void onDestroy() {
        digitClassifier0.close();
        digitClassifier1.close();
        digitClassifier2.close();
        digitClassifier3.close();

        super.onDestroy();
    }
>>>>>>> Stashed changes
}