package com.jwd.vlcplayer.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by scheet on 2017/10/26.
 */

public class TitleTypeTask extends AsyncTask<Void, Void, String> {
    private String TAG = "TitleTypeTask";

    private String typeRequest;
    private Handler mHandler;

    public TitleTypeTask(String typeRequest, Handler mHandler) {
        super();
        this.typeRequest = typeRequest;
        this.mHandler = mHandler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... param) {// 处理图片上传 ...

        //先将参数放入List，再对参数进行URL编码
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("tags", typeRequest));
        Log.e("<--->", "typeRequest = " + typeRequest);
//        //对参数编码
//        String urlParam = URLEncodedUtils.format(params, "UTF-8");
//        Log.e("<--->", "urlParam = " + urlParam);

        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet("http://192.168.1.61:8000/video/gettags/?tags=" + typeRequest);
        Log.e(TAG, "获取图片URL的结果：" + "http://192.168.1.61:8000/video/gettags/?tags=" + typeRequest);


        try {
            HttpResponse response = httpClient.execute(httpGet); //发起POST请求
            /**读取服务器返回过来的json字符串数据**/
            String result = EntityUtils.toString(response.getEntity(), "utf-8");

            Message msg = mHandler.obtainMessage();
            msg.what = 100;
            msg.obj = result;
            mHandler.sendMessage(msg);
            return "";
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message msg = mHandler.obtainMessage();
        msg.what = 101;
        mHandler.sendMessage(msg);
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}


