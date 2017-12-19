package com.jwd.vlcplayer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jwd.vlcplayer.R;
import com.jwd.vlcplayer.VideoPlayerActivity;
import com.jwd.vlcplayer.adapter.FileListAdapter;
import com.jwd.vlcplayer.bean.NewsEntity;
import com.jwd.vlcplayer.server.DBHelper;
import com.jwd.vlcplayer.server.DBManage;
import com.jwd.vlcplayer.utils.FileInfo;
import com.jwd.vlcplayer.utils.FileScanner;
import com.jwd.vlcplayer.utils.FileUrlRefreshTask;
import com.jwd.vlcplayer.utils.FileUrlUploadTask;
import com.jwd.vlcplayer.view.HeadListView;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener
        , SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = "NewsFragment";
    Activity activity;
    ArrayList<NewsEntity> newsList = new ArrayList<NewsEntity>();
    HeadListView mListView;
    // 请求的标签
    String videoRequest;
    // 标签是否变化
    String isChange;
    int channel_id;
    ImageView detail_loading;
    public final static int SET_NEWSLIST = 0;
    //Toast提示框
    private RelativeLayout notify_view;
    private TextView notify_view_text;
    private SwipeRefreshLayout swipeRefreshLayout;


    private List<FileInfo> mFileInfo = new ArrayList<>();
    private FileListAdapter mFileListAdapter;
    //add by garnet
    private Context mContext;

    private final int GET_VIDEO_INFO = 105;
    private final int REFRESH_VIDEO_INFO = 106;
    private final int GET_INFO_SUCCESS = 7;
    private final int GET_INFO_ERROR = 8;

    private DBManage mDBManage;
    private DBHelper mDBHelper;


    @Override
    public void onRefresh() {
        handler.sendEmptyMessage(REFRESH_VIDEO_INFO);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        mContext = getActivity();
        Bundle args = getArguments();
        videoRequest = args != null ? args.getString("text") : "";
        isChange = args != null ? args.getString("isChange") : "";
        Log.e(TAG, "=onCreateView =videoRequest =  " + videoRequest);
        Log.e(TAG, "=onCreateView =isChange =  " + isChange);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        Log.d(TAG, "=== onAttach =====");
        this.activity = activity;
        super.onAttach(activity);
    }

    /**
     * 此方法意思为fragment是否可见 ,可见时候加载数据
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            //fragment可见时加载数据
            if (newsList != null && newsList.size() != 0) {
                handler.obtainMessage(SET_NEWSLIST).sendToTarget();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        handler.obtainMessage(SET_NEWSLIST).sendToTarget();
                    }
                }).start();
            }
        } else {
            //fragment不可见时不执行操作
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, " === onCreateView ==== ");
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.news_fragment, null);
        mListView = (HeadListView) view.findViewById(R.id.mListView);
        mFileListAdapter = new FileListAdapter(activity, mFileInfo);
        mListView.setAdapter(mFileListAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_info);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setDistanceToTriggerSync(100);// 设置手指在屏幕下拉多少距离会触发下拉刷新
        swipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT); // 设置圆圈的大小
        detail_loading = (ImageView) view.findViewById(R.id.detail_loading);
        //Toast提示框
        notify_view = (RelativeLayout) view.findViewById(R.id.notify_view);
        notify_view_text = (TextView) view.findViewById(R.id.notify_view_text);

        if(null== mDBManage){
            mDBManage = new DBManage(mContext);
        }
        if(null==mDBHelper){
            mDBHelper = new DBHelper(mContext);
        }
        Log.e(TAG, " === 向服务器请求视频的数据==== ");
        handler.sendEmptyMessage(GET_VIDEO_INFO);

        return view;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case SET_NEWSLIST:
                    break;
                case GET_VIDEO_INFO:
                    //获取服务器中所有的图片和视频的链接
                    if (null == videoRequest) {
                        Bundle args = getArguments();
                        videoRequest = args != null ? args.getString("text") : "";
                        isChange = args != null ? args.getString("isChange") : "";
                    }
                    Log.e(TAG, "GET_VIDEO_INFO videoRequest = " + videoRequest);
                    Log.e(TAG, "GET_VIDEO_INFO isChange = " + isChange);

                    String openId = "DBX520";
                    String videoKind = ifEvent(videoRequest);

                    if ("推荐".equals(videoRequest) && "true".equals(isChange)) {
                        //获取视频和图片信息
                        new FileUrlUploadTask(openId, videoKind, handler).execute();
                    }
                    isChange = "false";
                    //从数据库获取标签类型的数据
                    String infoDB = getDB(videoRequest);
                    if (null == infoDB || "".equals(infoDB)) {
                        //获取视频和图片信息
                        new FileUrlUploadTask(openId, videoKind, handler).execute();
                    } else {
                        if (infoDB.contains("SUCCESS")) {
                            JSONArray jsonArray = new JSONArray();
                            try {
                                JSONObject jsonObject = JSONObject.parseObject(infoDB);
                                jsonArray = jsonObject.getJSONArray("Data");
                            } catch (Exception e) {
                                Log.e(TAG, "JSONObject parseObject = " + e.getMessage());
                            }
                            List<FileInfo> mList = FileScanner.getJSONArray(jsonArray, false);
                            if (null != mFileInfo) {
                                mFileInfo.clear();
                            }
                            mFileInfo.addAll(mList);
                            if (null != mFileListAdapter) {
                                mFileListAdapter.notifyDataSetChanged();
                            }
                        } else
                            return;
                    }
                    break;
                case REFRESH_VIDEO_INFO:
                    if (null == videoRequest) {
                        Bundle args = getArguments();
                        videoRequest = args != null ? args.getString("text") : "";
                    }
                    Log.e(TAG, "REFRESH_VIDEO_INFO videoRequest = " + videoRequest);

                    String useId = "DBX520";
                    String refresh = ifEvent(videoRequest);
                    //刷新获取到的视频和图片信息
                    new FileUrlRefreshTask(useId, refresh, handler).execute();
                    if (null != swipeRefreshLayout) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case GET_INFO_SUCCESS:
                    if (null != swipeRefreshLayout) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    //获取信息成功
                    String urlInfo = (String) msg.obj;

                    //将从服务器请求到的数据写入数据库
                    mDBManage.insertData(videoRequest, urlInfo);

                    //解析服务器获取到的数据
                    if (urlInfo.contains("SUCCESS")) {
                        JSONArray jsonArray = new JSONArray();
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(urlInfo);
                            jsonArray = jsonObject.getJSONArray("Data");
                        } catch (Exception e) {
                            Log.e(TAG, "JSONObject parseObject = " + e.getMessage());
                        }
                        List<FileInfo> mList = FileScanner.getJSONArray(jsonArray, false);
                        if (null != mFileInfo) {
                            mFileInfo.clear();
                        }
                        mFileInfo.addAll(mList);
                        if (null != mFileListAdapter) {
                            mFileListAdapter.notifyDataSetChanged();
                        }
                    } else
                        return;
                    break;
                case GET_INFO_ERROR:
                    if (null != swipeRefreshLayout) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /* 摧毁视图 */
    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        Log.d(TAG, "== onDestroyView = ");
        if (null != videoRequest)
            videoRequest = null;
        if (null != mFileListAdapter)
            mFileListAdapter = null;
        if (mDBHelper != null)
            mDBHelper.close();
        if (mDBManage != null)
            mDBManage = null;
    }

    /* 摧毁该Fragment，一般是FragmentActivity 被摧毁的时候伴随着摧毁 */
    @Override
    public void onDestroy() {
        Log.d(TAG, "=== onDestroy ===  ");
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "channel_id = " + channel_id);
    }

    //跳转到播放界面
    private void goToVideoPlayerPage(String videoUrl) {
        Intent appIntent = new Intent(activity, VideoPlayerActivity.class);
        appIntent.putExtra(VideoPlayerActivity.VIDEO_PATH, videoUrl);
        startActivity(appIntent);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        FileInfo finfo = (FileInfo) (mFileListAdapter.getItem(position));
        String videoUrl = finfo.videoUrl;
        if (videoUrl != null && videoUrl != "") {
            Log.e(TAG, "onItemClick videoUrl = " + videoUrl);
            goToVideoPlayerPage(videoUrl);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        FileInfo finfo = (FileInfo) (mFileListAdapter.getItem(position));
        String videoUrl = finfo.videoUrl;
        Log.e(TAG, "onItemLongClick videoUrl = " + videoUrl);
        if (videoUrl != null && videoUrl != "") {
            goToVideoPlayerPage(videoUrl);
        }
        return true;
    }

    //将对应的文字转化为接口名称
    public String ifEvent(String typeRequest) {
        String videoType = null;

        if (typeRequest.equals("推荐")) {
            videoType = "Recommend";
        } else if (typeRequest.equals("电影")) {
            videoType = "Movie";
        } else if (typeRequest.equals("搞笑")) {
            videoType = "Funny";
        } else if (typeRequest.equals("体育")) {
            videoType = "Sport";
        } else if (typeRequest.equals("娱乐")) {
            videoType = "Amusement";
        } else if (typeRequest.equals("生活")) {
            videoType = "Society";
        } else if (typeRequest.equals("广告")) {
            videoType = "Ad";
        }
        return videoType;
    }

    private String getDB(String type) {
        String string = null;
        string = mDBManage.getData(type);
        Log.e(TAG,"getDB string =" +string);
        return string;
    }
}
