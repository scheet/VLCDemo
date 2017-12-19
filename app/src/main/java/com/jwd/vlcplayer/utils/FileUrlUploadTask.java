package com.jwd.vlcplayer.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by scheet on 2017/10/26.
 */

public class FileUrlUploadTask extends AsyncTask<Void, Void, String> {
    private String TAG = "FileUrlUploadTask";
    private String openid;
    private String urlPath;
    private String typeRequest;
    private Handler mHandler;
    Map<String, String> createCloud = new HashMap<String, String>();
    private JSONObject jsonResult;
//    private String type;

    public FileUrlUploadTask(String openid,String typeRequest, Handler mHandler) {
        super();
        this.openid = openid;
        this.typeRequest = typeRequest;
        this.mHandler = mHandler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... param) {// 处理图片上传 ...
        //当前时间戳
        long currentTimeMillis = Tool.getCurrentTimeMillis();
        String sign = Tool.signByMD5(CustomConfig.WEIXIN_KEY + currentTimeMillis);

        //先将参数放入List，再对参数进行URL编码
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("kword", typeRequest));
        params.add(new BasicNameValuePair("time", "" + currentTimeMillis));
        params.add(new BasicNameValuePair("sign", sign));
        params.add(new BasicNameValuePair("openid", openid));


        //对参数编码
        String urlParam = URLEncodedUtils.format(params, "UTF-8");
        Log.e("<--->", "urlParam = " + urlParam);

        HttpClient httpClient = new DefaultHttpClient();

        // 实例化post请示
//        HttpGet httpGet = new HttpGet("http://192.168.1.61:8000/video/" + "?" + urlParam);
        HttpGet httpGet = new HttpGet("http://192.168.1.61:8000/video/getlist/" + "?" + urlParam);
        Log.e(TAG, "获取图片URL的结果：" + "http://192.168.1.61:8000/video/getlist/" + "?" + urlParam);


        try {
            HttpResponse response = httpClient.execute(httpGet); //发起POST请求
            /**读取服务器返回过来的json字符串数据**/
            String result = EntityUtils.toString(response.getEntity(), "utf-8");

//            Log.e(TAG,"获取 result = "+result);

            Message msg = mHandler.obtainMessage();
            msg.what = 7;
            msg.obj = result;
            mHandler.sendMessage(msg);
            Log.e(TAG, " ==doInBackground== mHandler ==== " + mHandler);
            return "";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message msg = mHandler.obtainMessage();
        msg.what = 8;
        mHandler.sendMessage(msg);
        Log.e(TAG, " ==doInBackground== mHandler ==what = 9== " + mHandler);
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}


