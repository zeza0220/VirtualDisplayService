package com.txznet.virtualdisplayservice;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;

/**
 * 该类的主要作用为
 * 获取client端创建的对应display
 */
public class DisplayHelper implements DisplayManager.DisplayListener {
    private static final String TAG = "DisplayHelper";

    private DisplayManager displayManager;

    private Display hudDisplay;

    private Context context;
    private String displayName;
    private boolean isFirstRunHud=true;
    private boolean isEnable =true;

    private Listener listener;

    public DisplayHelper(Context context,String displayName,Listener listener){
        this.context=context;
        this.displayName=displayName;
        this.listener=listener;

        displayManager=(DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    /**
     * 开始监听display的生成
     */
    public void onResume(){
        getDisplayWithName();
        displayManager.registerDisplayListener(this,null);
    }

    /**
     * 暂停监听display的生成
     */
    public void onPause(){
        getDisplayWithName();
        displayManager.unregisterDisplayListener(this);
    }

    /**
     * 主动获取投屏
     */
    public void enable(){
        isEnable=true;
        getDisplayWithName();
    }

    /**
     * 主动停止投屏
     */
    public void disable(){
        isEnable=false;
        if (hudDisplay!=null){
            listener.cleanDisplay();
            hudDisplay=null;
        }
    }

    /**
     * 是否正在投屏
     * @return
     */
    public boolean isEnable(){
        return isEnable;
    }

    /**
     * 根据对应的display_name获取对应display
     */
    private synchronized void getDisplayWithName(){
        Display[] displays= getSystemDisplays(displayManager);
        Log.d(TAG, "getDisplayWithName: displays.length->"+displays.length);
        if (displays.length==0){
            if (hudDisplay!=null||isFirstRunHud){
                listener.cleanDisplay();
                hudDisplay=null;
            }
        }else {
            Display display=null;
            for (int i=0;i<displays.length;i++){
                Display item=displays[i];
                if (item!=null && item.isValid() && displayName.equals(item.getName())){
                    display=item;
                }
            }
            try {
                if (display!=null && display.isValid()){
                    if (hudDisplay==null){
                        listener.showDisplay(display);
                        hudDisplay=display;
                    }else if (! hudDisplay.getName().equals(displayName)){
                        listener.cleanDisplay();
                        listener.showDisplay(display);
                        hudDisplay=display;
                    }
                }else if (hudDisplay!=null){
                    listener.cleanDisplay();
                    hudDisplay=null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            isFirstRunHud=false;
        }
    }

    /**
     * 获取对应的displayManager
     * @param displayManager
     * @return
     */
    public Display[] getSystemDisplays(DisplayManager displayManager){
        return displayManager.getDisplays("android.hardware.display.category.PRESENTATION");
    }

    @Override
    public void onDisplayAdded(int displayId) {
        Log.d(TAG, "onDisplayAdded: displayId->"+displayId);
        getDisplayWithName();
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        Log.d(TAG, "onDisplayRemoved: displayId->"+displayId);
        getDisplayWithName();
    }

    @Override
    public void onDisplayChanged(int displayId) {
        Log.d(TAG, "onDisplayChanged: displayId->"+displayId);
        getDisplayWithName();
    }

    public interface Listener{
        void showDisplay(Display display);

        void cleanDisplay();
    }
}
