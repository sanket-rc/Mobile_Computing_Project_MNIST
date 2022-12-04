package com.android.mobile_application;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


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

    private void classifyDrawing(Bitmap bitmap) {
        if (bitmap != null)
        {
            Log.e(TAG, "Bitmap Here.");
        }
        if (digitClassifier.isInitialized())
        {
            Log.e(TAG, "Initialized Here.");
        }



        if ((bitmap != null) && (digitClassifier.isInitialized())) {
            try{
                responseText.setText(digitClassifier.classify(preprocessImage(bitmap, 0)));
                Log.e(TAG, "Classifying TL.");
            }
            catch (Exception e) {
                responseText.setText(e.toString());
                Log.e(TAG, "Error classifying TL.", e);
            }

            try{
                responseText1.setText(digitClassifier.classify(preprocessImage(bitmap, 1)));
                Log.e(TAG, "Classifying TR.");
            }
            catch (Exception e) {
                responseText1.setText(e.toString());
                Log.e(TAG, "Error classifying TR.", e);
            }

            try{
                responseText2.setText(digitClassifier.classify(preprocessImage(bitmap, 2)));
                Log.e(TAG, "Classifying BL.");
            }
            catch (Exception e) {
                responseText2.setText(e.toString());
                Log.e(TAG, "Error classifying BL.", e);
            }

            try{
                responseText3.setText(digitClassifier.classify(preprocessImage(bitmap, 3)));
                Log.e(TAG, "Classifying BR.");
            }
            catch (Exception e) {
                responseText3.setText(e.toString());
                Log.e(TAG, "Error classifying BR.", e);
            }
        }
    }

    private Bitmap preprocessImage(Bitmap bitmap, int version) {
        //0 = TL, 1 = TR, 2 = BL, 3 = BR
        Bitmap cropped;
        switch(version){
            case 0:
                cropped = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2, bitmap.getWidth()/2, bitmap.getHeight());
                break;
            case 1:
                cropped = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth(), bitmap.getHeight());
                break;
            case 2:
                cropped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth()/2, bitmap.getHeight()/2);
                break;
            default:
                cropped = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2, 0, bitmap.getWidth(), bitmap.getHeight()/2);
                break;
        }
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