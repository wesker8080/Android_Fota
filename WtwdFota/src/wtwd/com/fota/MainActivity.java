package wtwd.com.fota;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private final static String CHECK_KEY = "waterworld";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    TextView tvName, proText, filecode, versonNumber, size, releaseDate, versonNote, type;
    ProgressBar progressBar;
    Button start, pause, checkversion;
    String url;
    FileInfo fileInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvName = (TextView)findViewById(R.id.name);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        proText  = (TextView)findViewById(R.id.pro_text);
        filecode  = (TextView)findViewById(R.id.filecode);
        versonNumber  = (TextView)findViewById(R.id.versonNumber);
        size  = (TextView)findViewById(R.id.size);
        releaseDate  = (TextView)findViewById(R.id.releaseDate);
        versonNote  = (TextView)findViewById(R.id.versonNote);
        type  = (TextView)findViewById(R.id.type);
        start  = (Button)findViewById(R.id.start);
        pause  = (Button)findViewById( R.id.pause);
        checkversion  = (Button)findViewById( R.id.checkversion);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        start.setClickable(false);
        pause.setClickable(false);
        checkversion.setOnClickListener(this);
        init();
    }
    private void init() {
        verifyStoragePermissions(this);
        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
        proText.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        //创建文件信息对象
        url = "https://www.imooc.com/mobile/mukewang.apk";
        //fileInfo = new FileInfo(0, url, "mukewang.apk", 0, 0);
        fileInfo = downloadFota();
        tvName.setText(fileInfo.getFileName());
    }
    private FileInfo downloadFota() {
        String baseurl="https://www.waterworld.xin:8444/fota/downloadDeltaVersion?";
        String filecode="61";
        String time=""+System.currentTimeMillis();
        String token=MD5Util.getMD5(filecode+CHECK_KEY+time);
        String url=baseurl+"filecode="+filecode;
        url=url+"&"+"time="+time;
        url=url+"&"+"token="+token;
        return new FileInfo(0, url, "update.zip", 0, 0);
    }
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View mView) {
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        switch (mView.getId()){
            case R.id.start:
                start.setClickable(false);
                pause.setClickable(true);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileinfo", fileInfo);
                startService(intent);
                break;
            case R.id.pause:
                pause.setClickable(false);
                start.setClickable(true);
                intent.setAction(DownloadService.ACTION_PAUSE);
                intent.putExtra("fileinfo", fileInfo);
                startService(intent);
                break;
            case R.id.checkversion:
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkUpdate();
                    }
                }).start();*/
                WtwdFotaServer mWtwdFotaServer = new WtwdFotaServer();
                mWtwdFotaServer.checkUpdate(MainActivity.this, "MT6739", "android7.1", "yk915", "ddd", 1);
                break;
        }
    }

    private void checkUpdate() {
        String CHECKUPDATE_URL="https://www.waterworld.xin:8444/fota/checkUpdate";
        JSONObject json=new JSONObject();
        String hardware="MT6739";
        String system="android7.1";
        String board="yk915";
        String customer="ddd";

        int build=1;
        long time=System.currentTimeMillis();
        String token= MD5Util.getMD5(hardware+system+board+customer+build+CHECK_KEY+time);
        json.put("hardware", hardware);
        json.put("system", system);
        json.put("board", board);
        json.put("customer", customer);
        json.put("build", build);
        json.put("time", time);
        json.put("token", token);
        HttpConnectionUtil http = new HttpConnectionUtil();
        String result = http.postDataToServer(CHECKUPDATE_URL, json.toString());
		if (result != null) {
			Log.e(TAG, "post result : " + result);
			String mResult = JSONObject.parseObject(result).getString("result");
	        if (mResult != null && mResult.equals("1")) {
	            runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                	Integer mFilecode = JSONObject.parseObject(result).getInteger("filecode");
			            filecode.setText("filecode : " + mFilecode);
			            String mVersonNumber = JSONObject.parseObject(result).getString("versonNumber");
			            versonNumber.setText("versonNumber : " + mVersonNumber);
			            String mSize = JSONObject.parseObject(result).getString("size");
			            size.setText("size : " + mSize);
			            Long mReleaseDate = JSONObject.parseObject(result).getLong("releaseDate");
			            releaseDate.setText("releaseDate : " + mReleaseDate);
			            String mVersonNote = JSONObject.parseObject(result).getString("versonNote");
			            versonNote.setText("versonNote : " + mVersonNote);
			            String mType = JSONObject.parseObject(result).getString("type");
			            type.setText("type : " + mType);
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			            builder.setTitle("发现新版本");
			            builder.setMessage("filecode : " + mFilecode + "\n" +
			                    "versonNumber : " + mVersonNumber + "\n" +
			                    "size : " + mSize + "\n" +
			                    "releaseDate : " + mReleaseDate + "\n" +
			                    "versonNote : " + mVersonNote + "\n" +
			                    "type : " + mType);
	            		builder.setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog, int which) {
			                    pause.setClickable(true);
			                    Log.e(TAG, "立即下载");
			                    Intent intent = new Intent(MainActivity.this, DownloadService.class);
			                    intent.setAction(DownloadService.ACTION_START);
			                    intent.putExtra("fileinfo", fileInfo);
			                    startService(intent);
			                }
			            });
			            builder.setNegativeButton("取消下载", new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog, int which) {
			                    Log.e(TAG, "取消下载");
			                }
			            });
			                    builder.create().show();
			                }
			            });

	        }else {
				Log.e(TAG, "result code != 1");
			}
		}
        else {
            filecode.setText("无可用更新");
        }
    }
    private void download() {
        String baseurl="https://www.waterworld.xin:8444/fota/downloadDeltaVersion?";
        String filecode="61";
        String time=""+System.currentTimeMillis();
        String token=MD5Util.getMD5(filecode+CHECK_KEY+time);
        String url=baseurl+"filecode="+filecode;
        url=url+"&"+"time="+time;
        url=url+"&"+"token="+token;
        HttpConnectionUtil http = new HttpConnectionUtil();
        String result = http.postDataToServer(url, "");
        Log.e(TAG, "post result : " + result);
    }
    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                String fingerprint = intent.getStringExtra("fingerprint");
                String md5 = intent.getStringExtra("md5");
                boolean isComplete = intent.getBooleanExtra("complete", false);
                progressBar.setProgress(finished);
                proText.setText(new StringBuffer().append(finished).append("%"));
                if (isComplete) {
                    final File file = new File(DownloadService.DOWNLOAD_PATH + "/" + fileInfo.getFileName());
                    try {
                        String mMd5ByFile = MD5Util.getMd5ByFile(file);
                        Log.e(TAG, "mMd5ByFile : " + mMd5ByFile);
                        if (md5.equals(mMd5ByFile)) {
                            Log.e(TAG,"文件MD5校验成功");
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setTitle("Download Success");
		            		builder.setMessage("Update Now?");
							builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				                @Override
				                public void onClick(DialogInterface dialog, int which) {
				                    //Execute System Update
				                    updateFirmware(file);
				                }
				            });
							builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				                @Override
				                public void onClick(DialogInterface dialog, int which) {
				                    
				                }
				            });
							 builder.create().show();
                        }
                    } catch (FileNotFoundException mE) {
                        mE.printStackTrace();
                    }
                }
            }
        }
    };

	public void updateFirmware(File packageFile) {
		if (!packageFile.exists()) {
			Log.d(TAG, "packageFile not exists");
			return ;
		}
		
		try {
			Log.d(TAG, "installPackage");
			RecoverySystem.installPackage(this, packageFile);
			
		} catch (Exception e) {
			Log.e(TAG, "install  failure : " + e.toString());
			e.printStackTrace();
		}
	}

	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }



}
