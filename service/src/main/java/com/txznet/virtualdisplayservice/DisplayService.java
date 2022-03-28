package com.txznet.virtualdisplayservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DisplayService extends Service implements DisplayHelper.Listener {
    private static final String TAG = "DisplayService";

    private static String DISPLAY_NAME = "txz_display";

    private Context displayContext;
    private DisplayHelper displayHelper;
    private WindowManager windowManager;
    private Handler myHandler;

    private int displayWidth;
    private int displayHeight;

    private TextView textView;

    private boolean isDestroy=false;

    private Timer timer;
    SimpleDateFormat format;
    public DisplayService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        isDestroy=false;
        myHandler=new Handler(getMainLooper());
        displayHelper=new DisplayHelper(this,DISPLAY_NAME,this);
        format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        displayHelper.onResume();

    }

    @Override
    public void onDestroy() {
        isDestroy=true;
        if (myHandler!=null){
            myHandler.removeCallbacksAndMessages(null);
            myHandler=null;
        }

        if (displayHelper!=null){
            displayHelper.onPause();
        }
        super.onDestroy();
    }

    @Override
    public void showDisplay(Display display) {
        Log.d(TAG, "showDisplay: ");
        displayContext=createHUDDisplayContext(display);
        windowManager=(WindowManager) displayContext.getSystemService(Context.WINDOW_SERVICE);

        try{
            setDisplay(display);
            textView = new TextView(displayContext);
            textView.setBackgroundColor(Color.BLACK);
            timer=new Timer();
            timer.schedule(timerTask,0,1000);
            windowManager.addView(textView,buildLayoutParams());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            String time=format.format(new Date(System.currentTimeMillis()));
            Log.d(TAG, "run: "+time);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(time);
                }
            });
        }
    };

    @Override
    public void cleanDisplay() {
        removeView(textView);
    }

    private Context createHUDDisplayContext(Display display){
        Context disContext=createDisplayContext(display);
        final WindowManager wm=(WindowManager) disContext.getSystemService(Context.WINDOW_SERVICE);
        return new ContextThemeWrapper(disContext,0){
            @Override
            public Object getSystemService(String name) {
                return name.equals(Context.WINDOW_SERVICE) ? wm:super.getSystemService(name);
            }
        };
    }
    private void setDisplay(Display display){
        DisplayMetrics displayMetrics=new DisplayMetrics();
        if (display!=null){
            display.getMetrics(displayMetrics);
            displayWidth=displayMetrics.widthPixels;
            displayHeight=displayMetrics.heightPixels;
        }
    }

    private WindowManager.LayoutParams buildLayoutParams(){
        return new WindowManager.LayoutParams(
                -1,-1,0,0,getWindowType(),0,-1);
    }

    private int getWindowType(){
        return Build.VERSION.SDK_INT >= 26 ?2038:2005;
    }

    private void removeView(View view){
        if (view != null){
            try {
                windowManager.removeView(view);
                view=null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}