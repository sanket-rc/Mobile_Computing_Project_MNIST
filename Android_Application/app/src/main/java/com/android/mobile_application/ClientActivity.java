package com.android.mobile_application;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import java.net.InetAddress;

public class ClientActivity extends Activity {

    final static String SERVICE_TYPE = "_http._tcp.";

    NsdHelper nsdHelper;
    EditText hostIPText;
    EditText hostPortText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clientactivity);

        nsdHelper = new NsdHelper(this);
        hostIPText = findViewById(R.id.hostIP);
        hostPortText = findViewById(R.id.hostPort);

        nsdHelper.discoverServices();

        //InetAddress hostIP = nsdHelper.mService.getHost();
        //int hostPort = nsdHelper.mService.getPort();

        //hostIPText.setText(hostIP.toString());
        //hostPortText.setText(hostPort);

    }
}
