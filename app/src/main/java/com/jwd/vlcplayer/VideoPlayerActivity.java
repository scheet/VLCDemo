package com.jwd.vlcplayer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vlcplayer.VlcControllerView;
import com.vlcplayer.VlcMediaController;
import com.vlcplayer.VlcVideoView;

import org.videolan.vlc.listener.FullScreenListener;


public class VideoPlayerActivity extends Activity implements View.OnClickListener {

    VlcVideoView videoView;
    VlcMediaController controller;
    public static final String VIDEO_PATH = "videoName";
    /**
     * 当前进度
     */
    private Long currentPosition = (long) 0;
    private String mVideoPath = "";
//    String path = "http://baobab.wdjcdn.com/1456317490140jiyiyuetai_x264.mp4";

    private LinearLayout mLoadingLayout;
    private ImageView mLoadingImg;
    private ObjectAnimator mOjectAnimator;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 100:
                    stopLoadingAnimator();
                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        getDataFromIntent();
        initViews();

    }


    @Override
    protected void onStart() {
        super.onStart();
        initVideoSettings();
    }

    private void getDataFromIntent() {
        Intent Intent = getIntent();
        if (Intent != null && Intent.getExtras().containsKey(VIDEO_PATH)) {
            mVideoPath = Intent.getExtras().getString(VIDEO_PATH);
            Log.e("getDataFromIntent", "onItemClick mVideoPath = " + mVideoPath);
        }
    }

    private void initViews() {
        VlcControllerView controllerView = (VlcControllerView) findViewById(R.id.controllerView);
        videoView = (VlcVideoView) findViewById(R.id.videoView);
        controller = new VlcMediaController(controllerView, videoView,mHandler);
        mLoadingLayout = (LinearLayout) findViewById(R.id.loading_LinearLayout);
        mLoadingImg = (ImageView) findViewById(R.id.loading_image);
        startLoadingAnimator();

    }


    private void initVideoSettings() {
        videoView.setMediaListenerEvent(controller);
        videoView.startPlay(mVideoPath);
        controller.setFullScreenListener(new FullScreenListener() {
            @Override
            public void Fullscreen(boolean fullscreen) {
                if (fullscreen) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

//        controller.setSnapshotListener(new SnapshotListener() {
//            @Override
//            public void Snapshotscreen(boolean ifSnapshot) {
//                if (ifSnapshot) {
//                    getCurrentFrame();
//                }
//            }
//        });


    }


//    /**
//     * 获取视频当前帧
//     *
//     * @return
//     */
//    public Bitmap getCurrentFrame() {
//        if (videoView != null) {
//            MediaPlayer mediaPlayer = videoView.getmMediaPlayer();
//            return mediaPlayer.getCurrentFrame();
//        }
//        return null;
//    }


    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
        stopLoadingAnimator();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoView!=null) {
            videoView.onDestory();
            videoView = null;
        }
    }

    @Override
    public void onClick(View v) {

    }


    @NonNull
    private void startLoadingAnimator() {
        if (mOjectAnimator == null) {
            mOjectAnimator = ObjectAnimator.ofFloat(mLoadingImg, "rotation", 0f, 360f);
        }
        mLoadingLayout.setVisibility(View.VISIBLE);

        mOjectAnimator.setDuration(1000);
        mOjectAnimator.setRepeatCount(-1);
        mOjectAnimator.start();
    }

    private void stopLoadingAnimator() {
        mLoadingLayout.setVisibility(View.GONE);
        mOjectAnimator.cancel();
    }
}
