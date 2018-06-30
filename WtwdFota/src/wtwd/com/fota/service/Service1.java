package wtwd.com.fota.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;


import java.util.Calendar;

import wtwd.com.fota.CommonUtils;
import wtwd.com.fota.DataCache;
import wtwd.com.fota.WtwdReceive;
import android.content.IntentFilter;

/**
 * Created by wesker on 2018/6/14 11:34.
 */

public class Service1 extends Service{

    private static final String TAG = "Service1";



    /**
     * 使用aidl 启动Service2
     */
    private StrongService startS2 = new StrongService.Stub() {
        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), Service2.class);
            getBaseContext().stopService(i);
        }

        @Override
        public void startService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), Service2.class);
            getBaseContext().startService(i);
        }
    };

    /**
     * 在内存紧张的时候，系统回收内存时，会回调OnTrimMemory， 重写onTrimMemory当系统清理内存时从新启动Service2
     */
    @Override
    public void onTrimMemory(int level) {
		/*
		 * 启动service2
		 */
        startService2();

    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"service1 destory");
        super.onDestroy();
        startService2();
    }

    @Override
    public void onCreate() {
    	IntentFilter intentFilter = new IntentFilter(); 
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE"); 
		registerReceiver(new WtwdReceive(), intentFilter);
        startService2();
    }

    /**
     * 判断Service2是否还在运行，如果不是则启动Service2
     */
    private void startService2() {

        boolean isRun = CommonUtils.isServiceWork(Service1.this,
                "wtwd.com.fota.service.Service2");
        if (!isRun) {
            try {
                startS2.startService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startTask();
        return START_STICKY;
    }
    public void startTask(){
        String mDeviceUpdateTime = DataCache.getInstance(getBaseContext()).getDeviceUpdateTime();
		//!CommonUtils.isBlank(mDeviceUpdateTime)
        if (!CommonUtils.isBlank(mDeviceUpdateTime)) {
            String[] mSplit = mDeviceUpdateTime.split("_");
            int dayOfWeek = Integer.parseInt(mSplit[0]);
            int hourOfDay = Integer.parseInt(mSplit[1]);
            int minute = Integer.parseInt(mSplit[2]);

			long firstTime = SystemClock.elapsedRealtime();  
			long systemTime = System.currentTimeMillis();
			
            Calendar calendar = Calendar.getInstance();  
			calendar.setTimeInMillis(System.currentTimeMillis());  
			//calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			calendar.set(Calendar.MINUTE, minute);  
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);  
			calendar.set(Calendar.SECOND, 0);  
			calendar.set(Calendar.MILLISECOND, 0);  

			long selectTime = calendar.getTimeInMillis();  

			if(systemTime > selectTime) {  
				Log.e(TAG, "systemTime > selectTime");
				calendar.add(Calendar.MINUTE, 1);  
				selectTime = calendar.getTimeInMillis();  
			}  
			 
			long time = selectTime - systemTime;  
			firstTime += time;  
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            long triggerAtTime = calendar.getTimeInMillis() + 10000;
            Intent intent2 = new Intent(this, WtwdReceive.class);
            intent2.setAction("com.wtwd.action.checkupdate");
			intent2.putExtra("isAlarm", true);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
			long cycleTime = 7*24*60*60*1000;
            manager.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, cycleTime, pi);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) startS2;
    }
}
