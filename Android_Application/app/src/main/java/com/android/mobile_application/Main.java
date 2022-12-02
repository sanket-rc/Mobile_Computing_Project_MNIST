package com.android.mobile_application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Main extends Activity {

    Button hostBtn, clientBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        hostBtn = (Button) findViewById(R.id.hostBtn);
        clientBtn = (Button) findViewById(R.id.clientBtn);

        hostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(Main.this, HostActivity.class));
            }
        });

        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startActivity(new Intent(Main.this, ClientActivity.class));
            }
        });
    }


}
