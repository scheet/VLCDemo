package com.jwd.vlcplayer.utils;

import android.content.Context;

/**
 * Created by EstarWu on 2017/7/20.
 */

public final class CustomConfig {
    public static String sWeChat_APP_ID = "wx4795040929a23a82";
    public static String sWeChat_APP_SECRET = "fd46f864a409c142bef84e184b80335e";

    public static String sQQ_GET_USER = "https://graph.qq.com/user/get_user_info?";
    public static String sQQ_APP_ID = "101392361";
    public static String sQQ_APP_SECRET = "175e697244453a21e8c5743df59d0835";

    //京华服务器的网址
    public static String WEIXIN_UPLOAD_URL = "http://apihk.car-boy.com.cn:6688/";
    public static final String WEIXIN_KEY = "tjEclUDGJs";
    public static final String WEIXIN_SET_URL = "http://apihk.car-boy.com.cn:6688/";

    public static boolean sFeatureWifi = true;
    public static boolean sFeatureWeChat = true;
    public static boolean sFeatureWeiBo = true;
    public static boolean sFeatureCommunity = true;
    public static boolean sFeatureVpaiShare = true;

    public CustomConfig(Context ctx) {
//        sWeChat_APP_ID = ctx.getResources().getString(R.string.wechat_app_id);
//        sWeChat_APP_SECRET = ctx.getResources().getString(R.string.wechat_app_secret);
//
//        sFeatureWeChat = ctx.getResources().getBoolean(R.bool.feature_wechat);
//        sFeatureWeChat = ctx.getResources().getBoolean(R.bool.feature_weibo);
//        sFeatureWeChat = ctx.getResources().getBoolean(R.bool.feature_community);
    }

}
