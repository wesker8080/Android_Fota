package wtwd.com.fota;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author MR.ZHANG
 * @create 2018-06-07
 */
public class HttpConnectionUtil {
    private static final String TAG = "HttpConnectionUtil";
    private HttpURLConnection conn_http = null;
    private HttpsURLConnection conn_https = null;
    private OutputStream out = null;
    private OutputStreamWriter wr = null;
    private InputStream in = null;
    private ByteArrayOutputStream baos = null;
    public  String postDataToServer(String linkurl, String data) {
        // TODO Auto-generated method stub
        if (null == linkurl || "".equals(linkurl)) {
            return null;
        }

        if (null == data || "".equals(data)) {
            return null;
        }

        String res = null;
        int trescode = -1;

        try {
            URL url = new URL(linkurl);

            Log.e(TAG, "postDataToServer(),linkurl : " + linkurl);
			Log.e(TAG, "postDataToServer(),data : " + data);
//			private HttpURLConnection conn_http = null;
//			private HttpsURLConnection conn_https = null;

            if(!linkurl.startsWith("https")) {
                conn_http = (HttpURLConnection) url.openConnection();
                //application/json
                //translateConn.setRequestProperty("User-Agent","YarakuZen PHP Client v1.0");
//				conn_http.setRequestProperty("ContentType","application/json; charset=" + "UTF-8");
                //conn_http.setRequestProperty("content-type","application/json");
//				conn_http.setRequestProperty("ContentType","application/json");
                conn_http.setRequestProperty("connection","keep-alive");
                conn_http.setRequestProperty("charset","UTF-8");
                conn_http.setRequestProperty("Content-Type","application/json");
                conn_http.setRequestMethod("POST");

                conn_http.setReadTimeout(15000);
                conn_http.setConnectTimeout(15000);
                conn_http.setDoInput(true);
                conn_http.setDoOutput(true);
                conn_http.setUseCaches(false);

                conn_http.connect();

                out = conn_http.getOutputStream();
                wr = new OutputStreamWriter(out);

                wr.write(data);
                wr.flush();


//				inreader = new InputStreamReader(in);
//				reader = new BufferedReader(inreader);

                trescode = conn_http.getResponseCode();

                if(200==trescode) {
                    in = conn_http.getInputStream();
                }
				Log.i(TAG, "postDataToServer(),trescode=" + trescode);
				Log.i(TAG, "closeAllConnect(),10,end");
            }
            else {

                conn_https = (HttpsURLConnection) url.openConnection();
                //application/json
//				conn_https.setRequestProperty("Accept","application/json; charset=" + "UTF-8");
//				conn_https.setRequestProperty("ContentType","application/json; charset=" + "UTF-8");
                conn_https.setRequestProperty("connection","keep-alive");
                conn_https.setRequestProperty("charset","UTF-8");
                conn_https.setRequestProperty("Content-Type","application/json");
//				conn_http.setRequestProperty("content-type","application/json");
                conn_https.setRequestMethod("POST");

                conn_https.setReadTimeout(15000);
                conn_https.setConnectTimeout(15000);
                conn_https.setDoInput(true);
                conn_https.setDoOutput(true);
                conn_https.setUseCaches(false);


                final X509TrustManager trustAllCert =
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                                                    return new java.security.cert.X509Certificate[]{};
                                return null;
                            }
                        };
                final SSLSocketFactory sslSocketFactory = new SSLSocketFactoryCompat(trustAllCert);
                conn_https.setSSLSocketFactory(sslSocketFactory);

////                SSLContext sslcontext = SSLContext.getInstance("TLS");
//                SSLContext sslcontext = SSLContext.getInstance("SSL");
//                //public static SSLContext getInstance(String protocol, String provider)
////                SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
//                sslcontext.init(null, new TrustManager[] { myX509TrustManager }, null);
//                SSLSocketFactory ssf = sslcontext.getSocketFactory();
//                conn_https.setSSLSocketFactory(ssf);

                conn_https.connect();

                out = conn_https.getOutputStream();
                wr = new OutputStreamWriter(out);

                wr.write(data);
                wr.flush();

//				inreader = new InputStreamReader(in);
//				reader = new BufferedReader(inreader);

                trescode = conn_https.getResponseCode();
                if(200==trescode) {
                    in = conn_https.getInputStream();
                }
            }

            Log.e(TAG, "postDataToServer(),trescode=" + trescode);
            if(200==trescode) {
                baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }

                String tdata = null;
                tdata = baos.toString("UTF-8");

                Log.i(TAG,"startWDataThread(),tdata="+tdata);

                res = tdata;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        closeAllConnect();

        return res;
    }
    private void closeAllConnect() {
        Log.i(TAG, "closeAllConnect(),1,start");
        try {
            if(null!=conn_http) {
                conn_http.disconnect();
                conn_http = null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        try {
            if(null!=conn_https) {
                conn_https.disconnect();
                conn_https = null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        try {
            if(null!=out) {
                out.close();
                out = null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        try {
            if(null!=wr) {
                wr.close();
                wr = null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

//		try {
//			if(null!=inreader) {
//				inreader.close();
//				inreader = null;
//			}
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}

//		try {
//			if(null!=reader) {
//				reader.close();
//				reader = null;
//			}
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}

        try {
            if(null!=baos) {
                baos.close();
                baos = null;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "closeAllConnect(),1,end");
    }
}
