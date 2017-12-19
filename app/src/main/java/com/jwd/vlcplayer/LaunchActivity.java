package com.jwd.vlcplayer;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jwd.vlcplayer.adapter.NewsFragmentPagerAdapter;
import com.jwd.vlcplayer.app.AppApplication;
import com.jwd.vlcplayer.bean.ChannelItem;
import com.jwd.vlcplayer.bean.ChannelManage;
import com.jwd.vlcplayer.fragment.NewsFragment;
import com.jwd.vlcplayer.tool.BaseTools;
import com.jwd.vlcplayer.tool.VLCDialog;
import com.jwd.vlcplayer.utils.ExceptionHandler;
import com.jwd.vlcplayer.utils.TitleTypeTask;
import com.jwd.vlcplayer.view.ColumnHorizontalScrollView;

import java.util.ArrayList;


public class LaunchActivity extends FragmentActivity {

    private  static final String TAG = "LaunchActivity";
    /**
     * 自定义HorizontalScrollView
     */
    private ColumnHorizontalScrollView mColumnHorizontalScrollView;
    LinearLayout mRadioGroup_content;
    LinearLayout ll_more_columns;
    RelativeLayout rl_column;
    private ViewPager mViewPager;
    private ImageView button_more_columns;
    private NewsFragmentPagerAdapter mAdapetr;
    private Context mContext;

    /**
     * 用户选择的新闻分类列表
     */
    private ArrayList<ChannelItem> userChannelList = new ArrayList<ChannelItem>();
    /**
     * 当前选中的栏目
     */
    private int columnSelectIndex = 0;
    /**
     * 左阴影部分
     */
    public ImageView shade_left;
    /**
     * 右阴影部分
     */
    public ImageView shade_right;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth = 0;
    /**
     * Item宽度
     */
    private int mItemWidth = 0;
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

    //标签是否变化
    private String isChange = "false";


    /**
     * 请求CODE
     */
    public final static int CHANNELREQUEST = 1;

    /**
     * 调整返回的RESULTCODE
     */
    public final static int CHANNELRESULT = 10;

    private AlertDialog mQuitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * catch unexpected error
         */

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        setContentView(R.layout.activity_main);
        mContext = this;
        mScreenWidth = BaseTools.getWindowsWidth(this);
        mItemWidth = mScreenWidth / 7;// 一个Item宽度为屏幕的1/7
        initViews();
        initMenus();

    }

    private void initViews() {
        Log.e(TAG, "===initViews==== ");

        //判断标签是否变化
        Intent intent = getIntent();
        if(null != intent && !("".equals(intent))) {
            isChange = intent.getStringExtra("isChange");
            Log.e(TAG, "===isChange==== " + isChange);
        }

        mColumnHorizontalScrollView = (ColumnHorizontalScrollView) findViewById(R.id.mColumnHorizontalScrollView);
        mRadioGroup_content = (LinearLayout) findViewById(R.id.mRadioGroup_content);
        ll_more_columns = (LinearLayout) findViewById(R.id.ll_more_columns);
        rl_column = (RelativeLayout) findViewById(R.id.rl_column);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mAdapetr = new NewsFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapetr);
        mViewPager.setOnPageChangeListener(pageListener);

        shade_left = (ImageView) findViewById(R.id.shade_left);
        shade_right = (ImageView) findViewById(R.id.shade_right);
        button_more_columns = (ImageView) findViewById(R.id.button_more_columns);
        button_more_columns.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (null != fragments) {

                    fragments.clear();
                }
                Intent intent_channel = new Intent(getApplicationContext(), ChannelActivity.class);
                startActivity(intent_channel);//ForResult , CHANNELREQUEST
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

            }
        });
        VLCDialog.Builder builder = new VLCDialog.Builder(mContext);
        builder.setTitle(R.string.quit_title);
        builder.setMessage(R.string.quit_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        mQuitDialog = builder.create();

        setChangelView();

        Log.e(TAG, "===initViews== end == ");
    }


    private void initMenus() {

        ImageView rlIcon1 = new ImageView(this);
        ImageView rlIcon4 = new ImageView(this);

        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.action_edit_light));
        rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.abc_ic_clear_search_api_holo_light));

    }


    /**
     * 当栏目项发生变化时候调用
     */
    private void setChangelView() {
        Log.e(TAG, "=== setChangelView ====== ");
        initColumnData();
        initTabColumn();
        initFragment();

        mViewPager.setCurrentItem(0);
    }

    /**
     * 获取Column栏目 数据
     */
    private void initColumnData() {
        if (null != userChannelList) {
            userChannelList.clear();
        }
        userChannelList.addAll((ArrayList<ChannelItem>) ChannelManage.getManage(AppApplication.getApp().getSQLHelper()).getUserChannel());
    }

    /**
     * 初始化Column栏目项
     */
    private void initTabColumn() {
        Log.e(TAG, "=== initTabColumn ===== ");
        mRadioGroup_content.removeAllViews();
        int count = userChannelList.size();
        mColumnHorizontalScrollView.setParam(this, mScreenWidth, mRadioGroup_content, shade_left, shade_right, ll_more_columns, rl_column);

        String score[] = new String[count];
        String sendServer = "";
        for (int i = 0; i < count; i++) {
            score[i] = sendServer(userChannelList.get(i).getName());
            sendServer += score[i] + ",";

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            TextView columnTextView = new TextView(this);
            columnTextView.setTextAppearance(this, R.style.top_category_scroll_view_item_text);
            columnTextView.setBackgroundResource(R.drawable.radio_buttong_bg);
            columnTextView.setGravity(Gravity.CENTER);
            columnTextView.setPadding(5, 5, 5, 5);
            columnTextView.setId(i);
            columnTextView.setText(userChannelList.get(i).getName());
            columnTextView.setTextColor(getResources().getColorStateList(R.color.top_category_scroll_text_color_day));
            if (columnSelectIndex == i) {
                columnTextView.setSelected(true);
            }
            columnTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
                        View localView = mRadioGroup_content.getChildAt(i);
                        if (localView != v)
                            localView.setSelected(false);
                        else {
                            localView.setSelected(true);
                            mViewPager.setCurrentItem(i);
                        }
                    }
                    Toast.makeText(getApplicationContext(), userChannelList.get(v.getId()).getName() + "   " + fragments.size(), Toast.LENGTH_SHORT).show();
                }
            });
            mRadioGroup_content.addView(columnTextView, i, params);
        }
        //传递标签的参数到后台
        new TitleTypeTask(sendServer, sendHandler).execute();
    }


    public String sendServer(String typeRequest) {
        String type = "";
        if (typeRequest.equals("推荐")) {
            type = "00";
        } else if (typeRequest.equals("电影")) {
            type = "01";
        } else if (typeRequest.equals("搞笑")) {
            type = "02";
        } else if (typeRequest.equals("体育")) {
            type = "03";
        } else if (typeRequest.equals("娱乐")) {
            type = "04";
        } else if (typeRequest.equals("生活")) {
            type = "05";
        } else if (typeRequest.equals("广告")) {
            type = "06";
        }
        return type;
    }

    /**
     * 选择的Column里面的Tab
     */
    private void selectTab(int tab_postion) {
        columnSelectIndex = tab_postion;
        for (int i = 0; i < mRadioGroup_content.getChildCount(); i++) {
            View checkView = mRadioGroup_content.getChildAt(tab_postion);
            int k = checkView.getMeasuredWidth();
            int l = checkView.getLeft();
            int i2 = l + k / 2 - mScreenWidth / 2;
            mColumnHorizontalScrollView.smoothScrollTo(i2, 0);
        }
        //判断是否选中
        for (int j = 0; j < mRadioGroup_content.getChildCount(); j++) {
            View checkView = mRadioGroup_content.getChildAt(j);
            boolean ischeck;
            if (j == tab_postion) {
                ischeck = true;
            } else {
                ischeck = false;
            }
            checkView.setSelected(ischeck);
        }
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        fragments.clear();//清空
        int count = userChannelList.size();
        Log.e("LaunchActivity", "initFragment count = " + count);
        for (int i = 0; i < count; i++) {
            Bundle data = new Bundle();
            data.putString("text", userChannelList.get(i).getName());
            data.putInt("id", userChannelList.get(i).getId());
            data.putString("isChange", isChange);
            Log.e("initData = ", "initFragment = " + userChannelList.get(i).getName());
            Log.e("initData = ", "initFragment data = " + data);
            NewsFragment newfragment = new NewsFragment();
            newfragment.setArguments(data);
            fragments.add(newfragment);

        }
        Log.e("initData = ", "fragments.size() = " + fragments.size());
        mAdapetr.notifyDataSetChanged();
        isChange = "false";
//        NewsFragmentPagerAdapter mAdapetr = new NewsFragmentPagerAdapter(getSupportFragmentManager(), fragments);
//        mViewPager.setAdapter(mAdapetr);
//        mViewPager.setOnPageChangeListener(pageListener);


    }


    /**
     * ViewPager切换监听方法
     */
    public ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            Log.e("pageListener", "= onPageScrollStateChanged arg0=" + arg0);
        }


        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            Log.e("pageListener", "= onPageScrolled arg0=" + arg0);
            Log.e("pageListener", "= onPageScrolled arg2=" + arg2);
        }

        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub
            Log.e("pageListener", "= onPageSelected position=" + position);
            mViewPager.setCurrentItem(position);
            selectTab(position);
        }
    };

    @Override
    public void onBackPressed() {
        mQuitDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case CHANNELREQUEST:
                if (resultCode == CHANNELRESULT) {
                    Log.e("LaunchActivity", "=== onActivityResult requestCode====" + requestCode);
                    Log.e("LaunchActivity", "=== onActivityResult resultCode====" + resultCode);
                    setChangelView();
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler sendHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 100:
                    break;
                case 101:
                    break;
                default:
                    break;
            }
        }
    };

}
