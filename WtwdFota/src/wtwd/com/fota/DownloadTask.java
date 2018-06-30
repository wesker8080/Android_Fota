package wtwd.com.fota;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author MR.ZHANG
 * @create 2018-06-05
 */
public class DownloadTask {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAOImpl mThreadDAO = null;
    private long mFinished = 0;
    public boolean isPause = false;

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mThreadDAO = new ThreadDAOImpl(mContext);
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mThreadDAO.getThread(mFileInfo.getUrl());
        Log.e("threadsize==", threadInfos.size() + "");
        ThreadInfo info;
        if (threadInfos.size() == 0) {
            //初始化线程信息
            info = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            info = threadInfos.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(info).start();
    }


    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo threadInfo;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            httpDownLoad();
        }
        
        private void httpDownLoad() {
            //向数据库插入线程信息
            Log.e("isExists==", mThreadDAO.isExists(threadInfo.getUrl(), threadInfo.getId()) + "");
            if (!mThreadDAO.isExists(threadInfo.getUrl(), threadInfo.getId())) {
                mThreadDAO.insertThread(threadInfo);
            }
            HttpURLConnection connection;
            RandomAccessFile raf;
            InputStream is;
			File file = null;
            try {
                URL url = new URL(threadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
				connection.setReadTimeout(25000);
                connection.setRequestMethod("GET");
                //设置下载位置
                long start = threadInfo.getStart() + threadInfo.getFinish();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());         
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += threadInfo.getFinish();
                Log.e("getResponseCode ===", connection.getResponseCode() + "");
                String fingerprint = connection.getHeaderField("fingerprint");
                String md5 = connection.getHeaderField("md5");
				file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
				//!fingerprint.equals(Build.FINGERPRINT) 
				if (false) {
					Log.e("DownloadTask", "fingerprint compare fail!");
					if (connection != null) {
						connection.disconnect();
					}
					CommonUtils.showToastInService(mContext, R.string.toast_fingerprint_error);
					if(file != null) {
					boolean isdelete = file.delete();
					Log.e("wesker","fingerprint fail delete file : " + isdelete);
					}
					return;
				}
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    Log.e("getContentLength==", connection.getContentLength() + "");
                    //读取数据
                    is = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = is.read(buffer)) != -1) {

                        //下载暂停时，保存进度
                        if (isPause) {
                            Log.e("mfinished==", mFinished + "");
                            mThreadDAO.updateThread(mFileInfo.getUrl(), mFileInfo.getId(), mFinished);
                            return;
                        }

                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度发送广播给Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 1000) {//减少UI负载
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", (int)(mFinished * 100 / mFileInfo.getLength()));
                            mContext.sendBroadcast(intent);
                            Log.e(" mFinished percent===", mFinished * 100 / mFileInfo.getLength() + "");
                        }

                    }
					Intent completeIntent = new Intent();
					completeIntent.setAction("com.wtwd.action.download.success");
                    completeIntent.putExtra("complete", true);
                    completeIntent.putExtra("fingerprint", fingerprint);
                    completeIntent.putExtra("md5", md5);
					completeIntent.putExtra("fileinfo", mFileInfo);
					mContext.sendBroadcast(completeIntent);
                    //intent.putExtra("finished",100);
                    //intent.putExtra("complete", true);
                    //intent.putExtra("fingerprint", fingerprint);
                    //intent.putExtra("md5", md5);
					//intent.putExtra("fileinfo", mFileInfo);
                    //mContext.sendBroadcast(intent);
                    //删除线程信息
                    mThreadDAO.deleteThread(mFileInfo.getUrl(), mFileInfo.getId());
                    is.close();
                }
                raf.close();
                connection.disconnect();
            } catch (Exception e) {
				Log.e("WtwdReceive","download fail : " + e.toString());
				if(file != null) {
					boolean isdelete = file.delete();
					Log.e("wesker","download fail delete file : " + isdelete);
				}
				CommonUtils.showDownloadFailDialog(mContext);
                e.printStackTrace();
            }
//            finally {
//                try {
//                    if (connection != null && raf != null && is != null) {
//                        is.close();
//                        raf.close();
//                        connection.disconnect();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
	
	}

}

