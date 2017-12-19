package com.jwd.vlcplayer.utils;

/**
 * Created by scheet on 2017/4/24.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Tool {
    public static final int GET_WX_DOMAIN = 11;
    public static final int GET_WX_TYPE = 12;


    public static void DisplayToast(Context context, String displayText){
        Toast.makeText(context, displayText, Toast.LENGTH_LONG).show();
    }


    /**
     * 判断是否为有效字符串(空或空字符串) 不为空对象不为空字符串返回真 是空返回 false
     *
     * @param
     *
     * @return
     */
    public static boolean isValidString(String str) {
        if (null != str && !"".equals(str.trim())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * "HH:mm"
     */
    public static String getTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date.getTime()).toString();
    }

    /**
     * "yyyy-MM-dd"
     */
    public static String getDate(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date.getTime()).toString();
    }

    public static String getDateAndTime(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return sdf.format(date.getTime()).toString();
    }

    /**
     * 整点报时
     * @return 若为整点，返回小时数（1..24），否则返回-1
     */
    public static int getTimeHour(){

        Calendar calendar = Calendar.getInstance();

        int hours = calendar.get(Calendar.HOUR_OF_DAY);//1..24
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // 整点报时
        if( minute == 0 && second == 0 ){
            return hours;
        }

        return -1;
    }
    public static void MemCpy(byte[] target, byte[] source, int start, int len){
        for (int i = 0; i < len; i++) {
            target[i] = source[ i + start ];
        }
    }

    /** 得到当前时间的秒数 */
    public static long getTimeInSeconds(){
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }


    /***
     * 获取当前时间戳
     * @return
     */
    public static long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }

    private static final long ONE_DAY = 86400;
    /*****
     * 距离微信操作发送图片/视频时间
     * 是否在48小时以内
     * @param date
     * @return 时间
     */
    public static boolean toDayOfOprtime(String date) {
        if (isValidString(date)) {

            date = date.replaceAll("/", "-");

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date sendDate = null;

            try {
                sendDate = dateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sendDate);

            long time = sendDate.getTime() / 1000;
            long now = new Date().getTime() / 1000;
            long ago = now - time;

            if (ago <= ONE_DAY * 2){
                return true;
            }
        } else {
            return false;
        }
        return false;
    }


    /***
     * MD5加密
     * @param str
     * @return
     */
    public static String signByMD5(String str) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().toLowerCase();
    }

    public static byte[] getBytes(short sh){
        byte[] bytes = ByteBuffer.allocate(2).putShort(sh).array();

        byte tmp = bytes[0];
        bytes[0] = bytes[1];
        bytes[1] = tmp;

        return bytes;
    }

    public static byte[] getBytes(int aint){
        byte[] bytes = ByteBuffer.allocate(4).putInt(aint).array();

        byte tmp = bytes[0];
        bytes[0] = bytes[3];
        bytes[3] = tmp;

        tmp = bytes[1];
        bytes[1] = bytes[2];
        bytes[2] = tmp;

        return bytes;
    }

    public static byte[] getBytes(String str){
        byte[] bytes = null;
        try {
            if(str.length()<=32){
                bytes = ByteBuffer.allocate(64).put(str.getBytes("gb2312")).array();
            }else{
                str = str.substring(0, 32);
                bytes = ByteBuffer.allocate(64).put(str.getBytes("gb2312")).array();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static byte[] getStringToBytes(String str){
        byte[] bytes = null;
        try {
            bytes = ByteBuffer.allocate(512).put(str.getBytes("gb2312")).array();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static String getBytesToString(byte[] bts){
        String str = null;
        try {
            str = new String(bts, "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static short bytesToShort(byte firstByte, byte secondByte){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(firstByte);
        bb.put(secondByte);
        return bb.getShort(0);
    }

    public static short bytesToShort(byte[] source, int start){
        return (short) ((( source[ start + 1 ] << 8) | source[ start ] & 0xff));
    }

    public static int bytesToInt(byte[] source, int start) {
        return source[start+3] << 24 | (source[start+2] & 0xFF) << 16 | (source[start+1] & 0xFF) << 8 | (source[start] & 0xFF);
    }

    /**
     * 判断是否为wifi make true current connect service is wifi
     *
     * @param mContext
     * @return
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private static final long ONE_MONTH = 2592000;


    /***
     * 文件转字节数组
     * @param filePath
     * @return
     */
    public static byte[] File2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            if(file.exists()){
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1)
                {
                    bos.write(b, 0, n);
                }
                fis.close();
                bos.close();
                buffer = bos.toByteArray();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }

    static SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");

    /***
     * 判断两个时间差是否大于20秒
     * @return
     */
    public static boolean toDateOfOprtime(String date1, String date2) {
        if (isValidString(date1) && isValidString(date2)) {

            date1 = date1.replaceAll("/", "-");
            date2 = date2.replaceAll("/", "-");

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date nowDate = null;
            Date oldDate= null;

            try {
                nowDate = dateFormat.parse(date1);
                oldDate = dateFormat.parse(date2);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(nowDate);
            calendar.setTime(oldDate);

            long nowTime = nowDate.getTime() / 1000;
            long oldTime = oldDate.getTime() / 1000;
            long ago = nowTime - oldTime;

            if (ago >= 20){
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    /***
     * 解析微信上传域名接口
     * @param
     * @return
     */
    public static String parserWXDomainInfo(String data){
        String result = "";

        try {
            JSONObject obj = new JSONObject(data);
            int code = obj.optInt("code");

            if(code == 1){
                result = obj.optString("domain");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /***
     * 解析微信类型接口
     * @param
     * @return
     */
    public static String parserWXTypeInfo(String data){
        String result = "";

        try {
            JSONObject obj = new JSONObject(data);
            int code = obj.optInt("code");

            if(code == 1){
                result = obj.optString("wxtype");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    /***
     * 解析上传微信返回接口
     * @param result
     * @return
     */
    public static int parserWXResultInfo(String result){
        int resCode = 0;

        try {
            JSONObject obj = new JSONObject(result);
            resCode = obj.optInt("code");

            if(resCode == 7){
                int errcode = obj.optInt("errcode");
                resCode = errcode;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resCode;
    }
}

