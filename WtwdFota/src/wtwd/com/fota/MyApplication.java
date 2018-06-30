package wtwd.com.fota;

import android.app.Application;
import android.content.Context;

/**
 * Created by wesker on 2018/6/19 15:48.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    /**
     * 获取全局上下文*/
    public static Context getContext() {
        return context;
    }
}
