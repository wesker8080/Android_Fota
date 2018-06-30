package wtwd.com.fota.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import wtwd.com.fota.CommonUtils;


/**
 * Created by wesker on 2018/6/14 13:34.
 */
public class Service2 extends Service {

	/**
	 * 使用aidl 启动Service1
	 */
	private StrongService startS1 = new StrongService.Stub() {

		@Override
		public void stopService() throws RemoteException {
			Intent i = new Intent(getBaseContext(), Service1.class);
			getBaseContext().stopService(i);
		}

		@Override
		public void startService() throws RemoteException {
			Intent i = new Intent(getBaseContext(), Service1.class);
			getBaseContext().startService(i);

		}
	};

	/**
	 * 在内存紧张的时候，系统回收内存时，会回调OnTrimMemory， 重写onTrimMemory当系统清理内存时从新启动Service1
	 */
	@Override
	public void onTrimMemory(int level) {
		startService1();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		startService1();
	}

	@SuppressLint("NewApi")
	public void onCreate() {
		startService1();
	}

	/**
	 * 判断Service1是否还在运行，如果不是则启动Service1
	 */
	private void startService1() {
		boolean isRun = CommonUtils.isServiceWork(Service2.this,
				"wtwd.com.fota.service.Service1");
		if (!isRun) {
			try {
				startS1.startService();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (IBinder) startS1;
	}

}
