package com.jwd.vlcplayer.bean;

import android.database.SQLException;
import android.util.Log;

import com.jwd.vlcplayer.dao.ChannelDao;
import com.jwd.vlcplayer.db.SQLHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelManage {
    public static ChannelManage channelManage;
    /**
     * 默认的用户选择频道列表
     * */
    public static List<ChannelItem> defaultUserChannels;
    /**
     * 默认的其他频道列表
     * */
    public static List<ChannelItem> defaultOtherChannels;
    private ChannelDao channelDao;
    /** 判断数据库中是否存在用户数据 */
    private boolean userExist = false;
    static {
        defaultUserChannels = new ArrayList<ChannelItem>();
        defaultOtherChannels = new ArrayList<ChannelItem>();
//        defaultUserChannels.add(new ServerItem(1, "热点", 1, 1));
//        defaultUserChannels.add(new ServerItem(2, "推荐", 2, 1));
//        defaultUserChannels.add(new ServerItem(3, "深圳", 3, 1));
//        defaultUserChannels.add(new ServerItem(4, "时尚", 4, 1));
//        defaultUserChannels.add(new ServerItem(5, "科技", 5, 1));
//        defaultUserChannels.add(new ServerItem(6, "体育", 6, 1));
//        defaultUserChannels.add(new ServerItem(7, "军事", 7, 1));
//        defaultOtherChannels.add(new ServerItem(8, "财经", 1, 0));
//        defaultOtherChannels.add(new ServerItem(9, "汽车", 2, 0));
//        defaultOtherChannels.add(new ServerItem(10, "房产", 3, 0));
//        defaultOtherChannels.add(new ServerItem(11, "社会", 4, 0));
//        defaultOtherChannels.add(new ServerItem(12, "情感", 5, 0));
//        defaultOtherChannels.add(new ServerItem(13, "女人", 6, 0));
//        defaultOtherChannels.add(new ServerItem(14, "旅游", 7, 0));
//        defaultOtherChannels.add(new ServerItem(15, "健康", 8, 0));
//        defaultOtherChannels.add(new ServerItem(16, "美女", 9, 0));
//        defaultOtherChannels.add(new ServerItem(17, "游戏", 10, 0));
//        defaultOtherChannels.add(new ServerItem(18, "数码", 11, 0));
//        defaultUserChannels.add(new ServerItem(19, "娱乐", 12, 0));

        defaultUserChannels.add(new ChannelItem(1, "推荐", 1, 0));
        defaultUserChannels.add(new ChannelItem(2, "生活", 2, 0));
        defaultUserChannels.add(new ChannelItem(3, "电影", 3, 0));
        defaultUserChannels.add(new ChannelItem(4, "搞笑", 4, 0));
        defaultUserChannels.add(new ChannelItem(5, "体育", 5, 0));
        defaultUserChannels.add(new ChannelItem(6, "广告", 6, 0));
        defaultUserChannels.add(new ChannelItem(7, "娱乐", 7, 0));
    }

    private ChannelManage(SQLHelper paramDBHelper) throws SQLException {
        if (channelDao == null)
            channelDao = new ChannelDao(paramDBHelper.getContext());
        // NavigateItemDao(paramDBHelper.getDao(NavigateItem.class));
        return;
    }

    /**
     * 初始化频道管理类
     * @param
     * @throws SQLException
     */
    public static ChannelManage getManage(SQLHelper dbHelper)throws SQLException {
        if (channelManage == null)
            channelManage = new ChannelManage(dbHelper);
        return channelManage;
    }

    /**
     * 清除所有的频道
     */
    public void deleteAllChannel() {
        channelDao.clearFeedTable();
    }
    /**
     * 获取其他的频道
     * @return 数据库存在用户配置 ? 数据库内的用户选择频道 : 默认用户选择频道 ;
     */
    public List<ChannelItem> getUserChannel() {
        Object cacheList = channelDao.listCache(SQLHelper.SELECTED + "= ?",new String[] { "1" });
        if (cacheList != null && !((List) cacheList).isEmpty()) {
            userExist = true;
            List<Map<String, String>> maplist = (List) cacheList;
            int count = maplist.size();
            List<ChannelItem> list = new ArrayList<ChannelItem>();
            for (int i = 0; i < count; i++) {
                ChannelItem navigate = new ChannelItem();
                navigate.setId(Integer.valueOf(maplist.get(i).get(SQLHelper.ID)));
                navigate.setName(maplist.get(i).get(SQLHelper.NAME));
                navigate.setOrderId(Integer.valueOf(maplist.get(i).get(SQLHelper.ORDERID)));
                navigate.setSelected(Integer.valueOf(maplist.get(i).get(SQLHelper.SELECTED)));
                list.add(navigate);
            }
            return list;
        }
        initDefaultChannel();
        return defaultUserChannels;
    }

    /**
     * 获取其他的频道
     * @return 数据库存在用户配置 ? 数据库内的其它频道 : 默认其它频道 ;
     */
    public List<ChannelItem> getOtherChannel() {
        Object cacheList = channelDao.listCache(SQLHelper.SELECTED + "= ?" ,new String[] { "0" });
        List<ChannelItem> list = new ArrayList<ChannelItem>();
        if (cacheList != null && !((List) cacheList).isEmpty()){
            List<Map<String, String>> maplist = (List) cacheList;
            int count = maplist.size();
            for (int i = 0; i < count; i++) {
                ChannelItem navigate= new ChannelItem();
                navigate.setId(Integer.valueOf(maplist.get(i).get(SQLHelper.ID)));
                navigate.setName(maplist.get(i).get(SQLHelper.NAME));
                navigate.setOrderId(Integer.valueOf(maplist.get(i).get(SQLHelper.ORDERID)));
                navigate.setSelected(Integer.valueOf(maplist.get(i).get(SQLHelper.SELECTED)));
                list.add(navigate);
            }
            return list;
        }
        if(userExist){
            return list;
        }
        cacheList = defaultOtherChannels;
        return (List<ChannelItem>) cacheList;
    }

    /**
     * 保存用户频道到数据库
     * @param userList
     */
    public void saveUserChannel(List<ChannelItem> userList) {
        for (int i = 0; i < userList.size(); i++) {
            ChannelItem channelItem = (ChannelItem) userList.get(i);
            channelItem.setOrderId(i);
            channelItem.setSelected(Integer.valueOf(1));
            channelDao.addCache(channelItem);
        }
    }

    /**
     * 保存其他频道到数据库
     * @param otherList
     */
    public void saveOtherChannel(List<ChannelItem> otherList) {
        for (int i = 0; i < otherList.size(); i++) {
            ChannelItem channelItem = (ChannelItem) otherList.get(i);
            channelItem.setOrderId(i);
            channelItem.setSelected(Integer.valueOf(0));
            channelDao.addCache(channelItem);
        }
    }

    /**
     * 初始化数据库内的频道数据
     */
    private void initDefaultChannel(){
        Log.d("deleteAll", "deleteAll");
        deleteAllChannel();
        saveUserChannel(defaultUserChannels);
        saveOtherChannel(defaultOtherChannels);
    }
}
