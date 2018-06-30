package wtwd.com.fota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RecoverySystem;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import android.os.SystemProperties;

import wtwd.com.fota.service.Service2;


/**
 * Created by wesker on 2018/6/14 14:51.
 */

public class WtwdReceive extends BroadcastReceiver {
    private static final String TAG = "WtwdReceive";
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String CHECK_UPDATE = "com.wtwd.action.checkupdate";
	private static final String DOWNLOAD_SUCCESS = "com.wtwd.action.download.success";
	private static final String NETWORK_STATUS_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
	
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "Receive Action --> " + intent.getAction());
        if (intent.getAction().equals(BOOT_COMPLETED)) {
            //first in, get random time of week
            if(DataCache.getInstance(context).isFirstIn()) {
                Random mRandom = new Random();
                int dayOfWeek = mRandom.nextInt(7) + 1;
                int hourOfDay = mRandom.nextInt(7);
                int minuteOfDay = mRandom.nextInt(60);
                StringBuilder timeBuilder = new StringBuilder();
                timeBuilder.append(dayOfWeek);
                timeBuilder.append("_");
                timeBuilder.append(hourOfDay);
                timeBuilder.append("_");
                timeBuilder.append(minuteOfDay);
                DataCache.getInstance(context).setDeviceUpdateTime(timeBuilder.toString());
                DataCache.getInstance(context).setFirstInfalse();
            }
            Intent i = new Intent(context, Service2.class);
            context.startService(i);
        } else if (intent.getAction().equals(CHECK_UPDATE)) {
            boolean mIsAlarm = intent.getBooleanExtra("isAlarm", false);
            if (false) {
                String mDeviceUpdateTime = DataCache.getInstance(context).getDeviceUpdateTime();
                if (!CommonUtils.isBlank(mDeviceUpdateTime)) {
                    String[] mSplit = mDeviceUpdateTime.split("_");
                    int dayOfWeek = Integer.parseInt(mSplit[0]);
                    int hourOfDay = Integer.parseInt(mSplit[1]);
                    int minute = Integer.parseInt(mSplit[2]);
                    Calendar mCalendar = Calendar.getInstance();
                    int week = mCalendar.get(Calendar.DAY_OF_WEEK);
                    int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    int min = mCalendar.get(Calendar.MINUTE);
                    if (dayOfWeek == week && hourOfDay == hour && minute == min) {
                        WtwdFotaServer mWtwdFotaServer = new WtwdFotaServer();
                        mWtwdFotaServer.checkUpdate(context, "MT6739", "android7.1", "yk915", "ddd", 1);
                    }
                }
            } else {
                //Connect server to see if there is any update
                WtwdFotaServer mWtwdFotaServer = new WtwdFotaServer();
				String customer = SystemProperties.get("ro.wtwd_fota_customer");
				int build = Integer.parseInt(SystemProperties.get("ro.wtwd_fota_build"));
                String mBoard = Build.BOARD;
                String mHardware = Build.HARDWARE;
                String mRelease = Build.VERSION.RELEASE;
                Log.e(TAG, "mHardware : " + mHardware + "mRelease : " + mRelease + "---board : " + mBoard + "customer : " + customer + "---build : " + build );
                if (CommonUtils.isBlank(customer) || CommonUtils.isBlank(build + "") || CommonUtils.isBlank(mBoard) ||
                        CommonUtils.isBlank(mHardware) || CommonUtils.isBlank(mRelease)) {
                    Log.e(TAG, "CheckUpdate Fail! System parameter is not correct!");
                    return;
                }
                mWtwdFotaServer.checkUpdate(context, mHardware, mRelease, mBoard, customer, build);
            }
        } else if (DOWNLOAD_SUCCESS.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                String fingerprint = intent.getStringExtra("fingerprint");
                final String md5 = intent.getStringExtra("md5");
                boolean isComplete = intent.getBooleanExtra("complete", false);
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileinfo");
                if (isComplete) {
                    final File file = new File(DownloadService.DOWNLOAD_PATH + "/" + fileInfo.getFileName());
                    try {
                        String mMd5ByFile = MD5Util.getMd5ByFile(file);
                        if (md5.equals(mMd5ByFile)) {
                            Log.e(TAG,"文件MD5校验成功");
                            CommonUtils.showUpdateNowDialog(context, file);
                        }
                    } catch (FileNotFoundException mE) {
                        mE.printStackTrace();
                    }
                }
        }else if(NETWORK_STATUS_CHANGE.equals(intent.getAction())) {
			int type = NetStatUtils.getNetWorkConnectionType(context);
			if (type != -1) {
				Log.e(TAG, "net type change : " + type);
			} else {
				CommonUtils.showToastInService(context,R.string.toast_network_unavailable);
			}
		}
    }
    private void updateFirmware(Context mContext,File packageFile) {
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
