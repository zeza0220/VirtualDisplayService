package com.txznet.virtualdisplayclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * 客户端接入demo
 * 注意：在实际开发中，如果涉及投屏的暂停和继续的话，建议不要直接销毁重建的对应的display
 * 在少部分场景下，投屏实例的重复销毁创建，可能会造成部分异常
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private static String DISPLAY_NAME = "txz_display";
    private static String SERVICE_ACTION ="com.txznet.virtualdisplayservice.action.DISPLAY";
    private static String SERVICE_PACKAGE ="com.txznet.virtualdisplayservice";

    private SurfaceView surfaceView;
    private Surface surface;

    private VirtualDisplay virtualDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                bindDisplayService();
            }
        },1000);
    }

    private void initView(){
        surfaceView=(SurfaceView) findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated: ");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: ");
                surface=holder.getSurface();
                createVirtualDisplay();
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.e(TAG, "surfaceDestroyed: ");
                // 投屏简单销毁方式可以直接release对应的display
                if (virtualDisplay!=null){
                    virtualDisplay.release();
                    virtualDisplay=null;
                }
            }
        });
    }

    /**
     * 根据服务端提供的对应的DISPLAY_NAME，来创建对应的虚拟屏幕
     */
    private void createVirtualDisplay(){
        DisplayMetrics metrics=new DisplayMetrics();
        WindowManager windowManager=(WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (windowManager !=null){
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }else {
            Log.e(TAG, "createVirtualDisplay: windowManager == null!");
            return;
        }
        DisplayManager displayManager=(DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager!=null){
            virtualDisplay=displayManager.createVirtualDisplay(DISPLAY_NAME,
                    metrics.widthPixels,metrics.heightPixels,metrics.densityDpi,surface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
                            |DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                            |DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);
            Log.d(TAG, "createVirtualDisplay: widthPixels->"+metrics.widthPixels+
                    ",heightPixels->"+metrics.heightPixels+
                    ",densityDpi->"+metrics.densityDpi);
        }else {
            Log.e(TAG, "createVirtualDisplay: displayManager == null!" );
        }
    }

    /**
     * 根据服务端提供对应投屏服务的action来绑定对应的服务
     */
    private void bindDisplayService(){
        Intent intent=new Intent();
        intent.setAction(SERVICE_ACTION);
        intent.setPackage(SERVICE_PACKAGE);

        bindService(intent,displayConnection,Context.BIND_AUTO_CREATE);

    }

    ServiceConnection displayConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            try {
                // 服务死亡监听，重连机制
                service.linkToDeath(displayConnectCallback,0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: ");
        }
    };

    private IBinder.DeathRecipient displayConnectCallback =new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            bindDisplayService();
        }
    };
}