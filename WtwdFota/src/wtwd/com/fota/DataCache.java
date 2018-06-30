package wtwd.com.fota;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wesker on 2017/11/2216:07.
 */

public class DataCache {
    private static final String SHARENAME = "wtwd";
    private static volatile DataCache sInstance;

    private static SharedPreferences sPreferences;

    private DataCache(Context context) {
        if (sPreferences == null) {
            sPreferences = context.getSharedPreferences(SHARENAME, Context.MODE_PRIVATE);
        }
    }
    public static DataCache getInstance(Context context) {
        DataCache instance = sInstance;
        if (sInstance == null) {
            synchronized (DataCache.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = new DataCache(context);
                    sInstance = instance;
                }
            }
        }
        return instance;
    }

    public boolean isFirstIn() {
        return sPreferences.getBoolean("isFirst", true);
    }

    public void setFirstInfalse() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean("isFirst", false);
        editor.commit();
    }
    public String getDeviceUpdateTime() {
        return sPreferences.getString("updateTime",null);
    }
    public void setDeviceUpdateTime(String time) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString("updateTime",time);
        editor.commit();
    }

    public String getDownloadPath() {
        return sPreferences.getString("downloadPath",null);
    }
    public void setDownloadPath(String path) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString("downloadPath",path);
        editor.commit();
    }
}
