package com.txznet.virtualdisplayservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static String SERVICE_ACTION ="com.txznet.virtualdisplayservice.action.DISPLAY";
    private static String SERVICE_PACKAGE ="com.txznet.virtualdisplayservice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PermissionUtils.checkFloatPermission(this)){
            startDisplayService();
        }else {
            PermissionUtils.requestFloatPermission(this,1002);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startDisplayService();
    }

    private void startDisplayService(){
        Intent intent=new Intent();
        intent.setAction(SERVICE_ACTION);
        intent.setPackage(SERVICE_PACKAGE);
        ServiceConnection connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected: ");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected: " );
            }
        };
        bindService(intent,connection,BIND_AUTO_CREATE);
    }
}