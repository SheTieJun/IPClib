package me.shetj.aidl;


import android.content.Context;
import android.util.Log;

public class ShareAIDLSDK  {

    public static String TAG = "ShareAIDLSDK";

    private static volatile ShareAIDLSDK mInstance;
    private IController mController ;
    private ShareAIDLSDK() {

    }

    public static ShareAIDLSDK getInstance() {
        if (mInstance == null) {
            synchronized(ShareAIDLSDK.class) {
                if (mInstance == null) {
                    mInstance = new ShareAIDLSDK();
                }
            }
        }
        return mInstance;
    }

    public int bindService(Context context, String packageName, String service) {
        this.mController = ControllerImp.getInstance();
        if (context == null) return 0;
        return mController.bindService(context,context.getPackageName(), packageName, service);
    }

    public int bindService(Context context,String clientName, String packageName, String service) {
        this.mController = ControllerImp.getInstance();
        return mController.bindService(context,clientName, packageName, service);
    }

    public void sendToServer(String msg) {
        if (mController != null){
            mController.sendToServer(msg);
        }else {
            Log.i(TAG, "error : sendToServer: u should bindService first");
        }
    }

    public void unBindService() {
        if (mController != null){
            mController.unBindService();
        }
    }

    public void setCallBack(ICallBack callBack) {
        if (mController != null){
            mController.setCallBack(callBack);
        }else {
            Log.i(TAG, "error : setCallBack: u should bindService first");
        }
    }


    public static void destroy(){
        if (mInstance != null){
            mInstance.unBindService();
        }
        ControllerImp.destroy();
        mInstance = null;
    }
}
