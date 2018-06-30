package wtwd.com.fota;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by wesker on 2018/6/13 16:54.
 */

public class WtwdFotaServer {
    private static final String TAG = "WtwdFotaServer";
    private static final String BASE_URL = "https://www.waterworld.xin:8444/fota";
    private static final String CHECK_UPDATE = "/checkUpdate";
    private static final String DOWNLOAD_FULL = "/downloadFullVersion?";
    private static final String DOWNLOAD_DELTA = "/downloadDeltaVersion?";
    private static final String CHECK_KEY = "waterworld";
	private Handler mHandler = new Handler(Looper.getMainLooper());
    public  void checkUpdate(final Context mContext, String hardware, String system, String board, String customer, int build) {
		if (NetStatUtils.getNetWorkConnectionType(mContext) == -1) {
			Log.e(TAG, "The NetWorkConnection is incorrect");
            CommonUtils.showToastInService(mContext,R.string.toast_network_unavailable);
            return ;
        }

        if (CommonUtils.isBlank(hardware) || CommonUtils.isBlank(system) ||
                CommonUtils.isBlank(board) || CommonUtils.isBlank(customer) || CommonUtils.isBlank(build + "")) {
            Log.e(TAG, "The parameter is incorrect");
            return ;
        }
        final JSONObject json = new JSONObject();
        long time = System.currentTimeMillis();
        String token = MD5Util.getMD5(hardware + system + board + customer + build + CHECK_KEY + time);
        json.put("hardware", hardware);
        json.put("system", system);
        json.put("board", board);
        json.put("customer", customer);
        json.put("build", build);
        json.put("time", time);
        json.put("token", token);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpConnectionUtil http = new HttpConnectionUtil();
                final String result = http.postDataToServer(BASE_URL + CHECK_UPDATE, json.toString());
                if (!CommonUtils.isBlank(result)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                                Log.e(TAG, "post --> checkUpdate success --> result : " + result);
                                String mResult = JSONObject.parseObject(result).getString("result");
                                if (mResult.equals("1")) {
                                    final Integer mFilecode = JSONObject.parseObject(result).getInteger("filecode");
                                    String mVersonNumber = JSONObject.parseObject(result).getString("versonNumber");
                                    final Long mSize = JSONObject.parseObject(result).getLong("size");
                                    Long mReleaseDate = JSONObject.parseObject(result).getLong("releaseDate");
                                    String mVersonNote = JSONObject.parseObject(result).getString("versonNote");
                                    final String mType = JSONObject.parseObject(result).getString("type");
                                    String mDownloadPath = DataCache.getInstance(mContext).getDownloadPath();
                                    File mFile = CommonUtils.fileIsExists(mDownloadPath, "update_" + mFilecode + ".zip");
                                    if (mFile != null) {
                                        CommonUtils.showUpdateNowDialog(mContext, mFile);
                                    } else {
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Find New Version");
                                        builder.setMessage("filecode : " + mFilecode + "\n" +
                                                "versonNumber : " + mVersonNumber + "\n" +
                                                "size : " + mSize + "\n" +
                                                "releaseDate : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(mReleaseDate)) + "\n" +
                                                "versonNote : " + mVersonNote + "\n" +
                                                "type : " + mType);
                                        builder.setPositiveButton("Download NOW", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (NetStatUtils.getNetWorkConnectionType(mContext) == -1) {
                                                    CommonUtils.showToastInService(mContext, R.string.toast_network_unavailable);
                                                    return;
                                                } else if (CommonUtils.readSystem() > mSize) {
                                                    DownloadService.DOWNLOAD_PATH = "/data";
                                                } else if (CommonUtils.readSDCard() > mSize) {
                                                    DownloadService.DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/system_update";
                                                } else {
                                                    CommonUtils.showToastInService(mContext, R.string.toast_storage_not_enough);
                                                    DataCache.getInstance(mContext).setDownloadPath("null");
                                                    return;
                                                }
                                                DataCache.getInstance(mContext).setDownloadPath(DownloadService.DOWNLOAD_PATH);
                                                Intent intent = new Intent(mContext, DownloadService.class);
                                                intent.setAction(DownloadService.ACTION_START);
                                                intent.putExtra("fileinfo", startDownloadUpdate(mFilecode, mType));
                                                mContext.startService(intent);
                                            }
                                        });
                                        builder.setNegativeButton("Cancel", null);
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                        alertDialog.setCanceledOnTouchOutside(false);
                                        alertDialog.show();
                                    }
                                } else if (mResult.equals("0")) {
									CommonUtils.showToastInService(mContext, R.string.toast_no_newversion);
								} else if (mResult.equals("-1")) {
									Log.e(TAG, "request parameter is not correct");
									CommonUtils.showToastInService(mContext, R.string.toast_fingerprint_error);
								} else if (mResult.equals("-2")) {
									Log.e(TAG, "token check fail");
									CommonUtils.showToastInService(mContext, R.string.toast_fingerprint_error);
								} 
                        }
                    });
                } else {
                    CommonUtils.showToastInService(mContext, R.string.toast_server_incorrect);
                }
            }
        }).start();
    }
	
    /**
     * download update package
     * @param filecode filecode
     * @param type package type
     * @return
     */
     public FileInfo startDownloadUpdate(int filecode, String type) {
         String time=""+System.currentTimeMillis();
         String token=MD5Util.getMD5(filecode+CHECK_KEY+time);
         StringBuilder mUrlBuilder = new StringBuilder();
         mUrlBuilder.append(BASE_URL);
         if (type.equals("delta")) {
             mUrlBuilder.append(DOWNLOAD_DELTA);
         } else if (type.equals("full")) {
             mUrlBuilder.append(DOWNLOAD_FULL);
         }
         mUrlBuilder.append("filecode=");
         mUrlBuilder.append(filecode);
         mUrlBuilder.append("&");
         mUrlBuilder.append("time=");
         mUrlBuilder.append(time);
         mUrlBuilder.append("&");
         mUrlBuilder.append("token=");
         mUrlBuilder.append(token);
         return new FileInfo(0, mUrlBuilder.toString(), "update_" + filecode + ".zip", 0, 0);
     }
}
