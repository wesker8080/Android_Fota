package wtwd.com.fota;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by wesker on 2018/6/13 17:44.
 */

public class CommonUtils {
    private static final String TAG = "CommonUtils";
    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static void showToast(Context mContext, int str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    public static void showToastInService(final Context mContext, final int str) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：com.beidian.test.service.BasicInfoService ）
     * @return
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public static int getBatteryPercent(Context mContext) {
        BatteryManager batteryManager = (BatteryManager) mContext.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return 0;
    }

    public static long readSDCard() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
            return availCount * blockSize;
        }
        return 0;
    }

    public static long readSystem() {
        File root = Environment.getRootDirectory();
        Log.e("wesker", "root.getPath() : " + root.getPath());
        StatFs sf = new StatFs("/data");
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        return availCount * blockSize;
    }

    //判断文件是否存在
    public static File fileIsExists(String path, String strFile) {
        try {
            File f = new File(path, strFile);
            if (f.exists()) {
                return f;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    public static void showUpdateNowDialog(final Context context, final File file) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Download Success");
        builder.setMessage("Update Now?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Execute System Update
                int mBatteryPercent = CommonUtils.getBatteryPercent(context);
                Log.d(TAG, "Current BatteryPercent --> " + mBatteryPercent);
                if (mBatteryPercent >= 30) {
                    updateFirmware(context, file);
                } else {
                    CommonUtils.showToastInService(context,R.string.toast_battery_low);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
    public static void showDownloadFailDialog(final Context context) {
		Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        builder.setTitle("Download Fail");
		        builder.setMessage("Make sure your network is already connected?");
		        builder.setPositiveButton("OK", null);
		        AlertDialog alertDialog = builder.create();
		        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		        alertDialog.setCanceledOnTouchOutside(false);
		        alertDialog.show();
            }
        });
    }
    private static void updateFirmware(Context mContext,File packageFile) {
        if (!packageFile.exists()) {
            Log.e(TAG, "packageFile not exists");
            return;
        }
        try {
            RecoverySystem.installPackage(mContext,packageFile);
        } catch (IOException mE) {
            Log.e(TAG, "Install SystemUpdate Package failure");
            mE.printStackTrace();
        }
    }
}
