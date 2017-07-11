package com.dji.geodemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;

public class ConnectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ConnectionActivity.class.getName();

    private TextView mTextConnectionStatus;
    private TextView mTextProduct;
    private Button mBtnOpen;
    private Button mBtnSend;

    /*Pola dodane*/
    private TextView mTxtResult;
    private EditText mEdTxtAdres;
    private EditText mEdTxtPort;

    String ADRES_SERVER_PORT = "192.168.0.103";
    int UDP_SERVER_PORT = 8040;
    String keyIdentifer = "ConnectionActivity";
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.VIBRATE,
                            android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.WAKE_LOCK, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                            android.Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_connection);

        initUI();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(GEODemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    private void runUdpClient()  {

        /* Przerzutujmy dodane pola w tej funkcji*/
        TextView wynik = (TextView) findViewById(R.id.textViewResult);
        EditText adres = (EditText) findViewById(R.id.editTextAdresIP);
        EditText port = (EditText) findViewById(R.id.editTextPortUDP);

        /*wpisz adres i port z pol tekstowych do zmiennych globalnych*/
        ADRES_SERVER_PORT = adres.getText().toString();
        UDP_SERVER_PORT = Integer.parseInt(port.getText().toString());

        if(count <= 255)
            count++;
        else
            count = 0;

        //Paczka w postaci: Licznik, Nazwa, Czas, Long, Lat, Alt;
        String udpMsg = String.valueOf(count)+",Nazwa_Urządzenia,12039.423423,52.21345,20.3414132,100.31";
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(ADRES_SERVER_PORT);
            DatagramPacket dp;
            dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, UDP_SERVER_PORT);
            ds.send(dp);
            wynik.setText("Wysłano...");
        } catch (SocketException e) {
            e.printStackTrace();
            wynik.setText("SocketException e");
        } catch (IOException e) {
            e.printStackTrace();
            wynik.setText("IOException e");
        } catch (Exception e) {
            e.printStackTrace();
            wynik.setText("Exception e");
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }

    private void initUI() {

        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        mTextProduct = (TextView) findViewById(R.id.text_product_info);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnSend = (Button) findViewById(R.id.button_send_udp);
        mTxtResult = (TextView) findViewById(R.id.textViewResult);
        mBtnSend.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        mBtnOpen.setEnabled(false);

        /*Dodane pola*/
        mEdTxtAdres = (EditText) findViewById(R.id.editTextAdresIP);
        mEdTxtPort = (EditText) findViewById(R.id.editTextPortUDP);
        mTxtResult = (TextView) findViewById(R.id.textViewResult);

    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();

        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = GEODemoApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");
            mBtnOpen.setEnabled(true);

            String str = mProduct instanceof Aircraft ? "Aircraft" : "HandHeld";
            mTextConnectionStatus.setText("Status: " + str + " connected");

            if (null != mProduct.getModel()) {
                mTextProduct.setText("" + mProduct.getModel().getDisplayName());
            } else {
                mTextProduct.setText(R.string.product_information);
            }

        } else {
            Log.v(TAG, "refreshSDK: False");
            mBtnOpen.setEnabled(false);

            mTextProduct.setText(R.string.product_information);
            mTextConnectionStatus.setText(R.string.connection_loose);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_open: {
                runUdpClient();
                Intent intent = new Intent(this, MainActivity.class);
                String msg2Main = ADRES_SERVER_PORT + "," + String.valueOf(UDP_SERVER_PORT);
                intent.putExtra(keyIdentifer,msg2Main);
                startActivity(intent);
                break;
            }

            case R.id.button_send_udp:{

                runUdpClient();



                break;
            }
            default:
                break;
        }
    }

}
