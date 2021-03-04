package me.shetj.sharedmemory;

import android.os.Build;

public class Utils {

    public static String TAG = "sharedMemory";
    public Utils() {
    }


    public static boolean isMoreThanAPI27() {
        return Build.VERSION.SDK_INT >= 27;
    }

    public static boolean isMoreThanAPI21() {
        return Build.VERSION.SDK_INT >= 21;
    }
}