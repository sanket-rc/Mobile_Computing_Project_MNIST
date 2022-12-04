package com.android.mobile_application;



import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;


public class ImageDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Initialize variables
    ImageView capturedImage;
    Spinner spinner;
    Button btnUploadImg;
    Bitmap bitmap;

    TextView responseText;
    TextView responseText1;
    TextView responseText2;
    TextView responseText3;
    Gateway gateway;
    HashMap<String, BluetoothSocket> map;
    List<String> accepteddevices;

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

        responseText = findViewById(R.id.responseText);
        responseText1 = findViewById(R.id.responseText1);
        responseText2 = findViewById(R.id.responseText2);
        responseText3 = findViewById(R.id.responseText3);
        map=Master.map;
        accepteddevices=Master.accepteddevices;

        Bundle bundle = getIntent().getExtras();

        // Retrieve the captured image
        if(bundle != null){
            Intent intent = getIntent();
            bitmap = (Bitmap) intent.getParcelableExtra("bitmapImage");
            capturedImage.setImageBitmap(bitmap);
        }

        // Listens for the upload button to get clicked
        btnUploadImg.setOnClickListener((view) -> classifyDrawing(bitmap));

        // Set up digit classifier
        try {
            digitClassifier.initialize();
            Log.e(TAG, "Setting up digit classifier.");
        }
        catch (Exception e){
            Log.e(TAG, "Error to setting up digit classifier.");
        }


    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void classifyDrawing(Bitmap bitmap) {

        if (bitmap != null) {
            Log.e(TAG, "Bitmap Here.");
        }
        if (digitClassifier.isInitialized()) {
            Log.e(TAG, "Initialized Here.");
        }

        if ((bitmap != null) && (digitClassifier.isInitialized())) {
            try {
                String bitmap_str = BitMapToString(preprocessImage(bitmap, 0));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("classify_image",1);
                    jsonObject.put("bitmap_string",bitmap_str);
                    jsonObject.put("quadrant", 0);
//                    jsonObject.put("matrix2", new JSONArray(matrixint2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonString = jsonObject.toString();
                //System.out.println(i+"->"+jsonString);


                gateway = new Gateway(map.get(accepteddevices.get(0)), Master.handler);
                gateway.write(jsonString.getBytes());
                responseText.setText(digitClassifier.classify(preprocessImage(bitmap, 0)));
                Log.e(TAG, "Classifying TL.");
            } catch (Exception e) {
                responseText.setText(e.toString());
                Log.e(TAG, "Error classifying TL.", e);
            }

            try {
                String bitmap_str = BitMapToString(preprocessImage(bitmap, 1));
                responseText1.setText(digitClassifier.classify(preprocessImage(bitmap, 1)));
                Log.e(TAG, "Classifying TR.");
            } catch (Exception e) {
                responseText1.setText(e.toString());
                Log.e(TAG, "Error classifying TR.", e);
            }

            try {
                responseText2.setText(digitClassifier.classify(preprocessImage(bitmap, 2)));
                Log.e(TAG, "Classifying BL.");
            } catch (Exception e) {
                responseText2.setText(e.toString());
                Log.e(TAG, "Error classifying BL.", e);
            }
            try {
                responseText3.setText(digitClassifier.classify(preprocessImage(bitmap, 3)));
                Log.e(TAG, "Classifying BR.");
            } catch (Exception e) {
                responseText3.setText(e.toString());
                Log.e(TAG, "Error classifying BR.", e);
            }
        }
    }

    private Bitmap preprocessImage(Bitmap bitmap, int version) {
        int width = bitmap.getWidth()/2;
        int height = bitmap.getHeight()/2;
        Bitmap bnw = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        int R, G, B;
        int pixel;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // get pixel color
                pixel = bitmap.getPixel(x, y);

                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                if (gray < 128) {
                    gray = 255;
                }
                else{
                    gray = 0;
                }
                // set new pixel color to output bitmap
                bnw.setPixel(x, y, Color.rgb(gray, gray, gray));
            }
        }

        Log.e(TAG, "Cropping.");
        //0 = TL, 1 = TR, 2 = BL, 3 = BR
        Bitmap cropped;
        switch(version){
            case 0:
                cropped = Bitmap.createBitmap(bnw, 0, 0, width, height);
                break;
            case 1:
                cropped = Bitmap.createBitmap(bnw, width, 0, width, height);
                break;
            case 2:
                cropped = Bitmap.createBitmap(bnw, 0, height, width, height);
                break;
            default:
                cropped = Bitmap.createBitmap(bnw, width, height, width, height);
                break;
        }
        Log.e(TAG, "Leaving Cropping.");
        return cropped;
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