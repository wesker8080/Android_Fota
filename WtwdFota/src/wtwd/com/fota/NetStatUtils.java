package wtwd.com.fota;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_MOBILE;

/**
 * Created by wesker on 2018/6/14 9:45.
 */

public class NetStatUtils {
    /**
     *
     * @return 是否有活动的网络连接
     */
    public final boolean hasNetWorkConnection(Context context){
        //获取连接活动管理器
        final ConnectivityManager connectivityManager= (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取链接网络信息
        final NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

        return (networkInfo!= null && networkInfo.isAvailable());

    }
    /**
     * @return 返回boolean ,是否为wifi网络
     *
     */
    public final boolean hasWifiConnection(Context context)
    {
        final ConnectivityManager connectivityManager= (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //是否有网络并且已经连接
        return (networkInfo!=null&& networkInfo.isConnectedOrConnecting());


    }

    /**
     * @return 返回boolean,判断网络是否可用,是否为移动网络
     *
     */

    public final boolean hasGPRSConnection(Context context){
        //获取活动连接管理器
        final ConnectivityManager connectivityManager= (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (networkInfo!=null && networkInfo.isAvailable());

    }
    /**
     * @return  判断网络是否可用，并返回网络类型，ConnectivityManager.TYPE_WIFI，ConnectivityManager.TYPE_MOBILE，不可用返回-1
     */
    public static int getNetWorkConnectionType(Context context){
         ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		 NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo(); 
		 if (networkInfo != null && networkInfo.isAvailable()) { 
		 	switch (networkInfo.getType()) { 
					case TYPE_MOBILE: 
						return 0; 
					case TYPE_WIFI: 
						return 1; 
					default: break; 
					} 
			}
		 return -1; 
    }
	
}
