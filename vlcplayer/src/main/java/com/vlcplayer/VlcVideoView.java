package com.vlcplayer;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import com.log.Logger;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.vlc.listener.MediaListenerEvent;
import org.videolan.vlc.listener.MediaPlayerControl;
import org.videolan.vlc.listener.VideoSizeChange;

public class VlcVideoView extends TextureView implements MediaPlayerControl, TextureView.SurfaceTextureListener, VideoSizeChange {
    private VlcVideoPlayer videoMediaLogic;
    private final String tag = "VlcVideoView";
    private MediaPlayer mMediaPlayer = null;

    public VlcVideoView(Context context) {
        this(context, null);
    }

    public VlcVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VlcVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        init(context);
    }

    public void setMediaListenerEvent(MediaListenerEvent mediaListenerEvent) {
        videoMediaLogic.setMediaListenerEvent(mediaListenerEvent);
    }

    @Override
    public boolean canControl() {
        return videoMediaLogic.canControl();
    }

    public void onStop() {
        videoMediaLogic.onStop();
    }

    public void onDestory() {
        if (videoMediaLogic != null)
            videoMediaLogic.setVideoSizeChange(null);
        Logger.i(tag, "onDestory");
    }

    private void init(Context context) {
        videoMediaLogic = new VlcVideoPlayer(context);
        videoMediaLogic.setVideoSizeChange(this);
        setSurfaceTextureListener(this);
    }

    public void setMediaPlayer(LibVLC libVLC) {
        videoMediaLogic.setMediaPlayer(libVLC);
    }

    public void setMedia(Media media) {
        videoMediaLogic.setMedia(media);
    }

    @Override
    public boolean isPrepare() {
        return videoMediaLogic.isPrepare();
    }


    @Override
    public void startPlay(String path) {
        videoMediaLogic.startPlay(path);
    }

    public void saveState() {
        videoMediaLogic.saveState();
    }

    @Override
    public void start() {
        videoMediaLogic.start();
    }

    @Override
    public void pause() {
        videoMediaLogic.pause();
    }

    @Override
    public long getDuration() {
        return videoMediaLogic.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return videoMediaLogic.getCurrentPosition();
    }

    @Override
    public void seekTo(long pos) {
        videoMediaLogic.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoMediaLogic.isPlaying();
    }


    @Override
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        if (mirror) {
            setScaleX(-1f);
        } else {
            setScaleX(1f);
        }
    }

    private boolean mirror = false;

    @Override
    public boolean getMirror() {
        return mirror;
    }


    @Override
    public int getBufferPercentage() {
        return videoMediaLogic.getBufferPercentage();
    }

    @Override
    public boolean setPlaybackSpeedMedia(float speed) {
        return videoMediaLogic.setPlaybackSpeedMedia(speed);
    }

    @Override
    public float getPlaybackSpeed() {
        return videoMediaLogic.getPlaybackSpeed();
    }


    @Override
    public void setLoop(boolean isLoop) {
        videoMediaLogic.setLoop(isLoop);
    }

    @Override
    public boolean isLoop() {
        return videoMediaLogic.isLoop();
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Logger.i(tag, "onSurfaceTextureAvailable");
        videoMediaLogic.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Logger.i(tag, "onSurfaceTextureSizeChanged");
        post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(mVideoWidth, mVideoHeight);
            }
        });
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logger.i(tag, "onSurfaceTextureDestroyed");
        videoMediaLogic.onSurfaceTextureDestroyed();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    //根据播放状态 打开关闭旋转动画
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Logger.i(tag, "onAttachedToWindow");
        if (isInEditMode()) {
            return;
        }
        setKeepScreenOn(true);
        if (videoMediaLogic != null)
            videoMediaLogic.onAttachedToWindow(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Logger.i(tag, "onDetachedFromWindow");
        if (isInEditMode()) {
            return;
        }
        setKeepScreenOn(false);
        if (videoMediaLogic != null)
            videoMediaLogic.onAttachedToWindow(false);
    }
    private boolean isRotation = true;
    public boolean isRotation() {
        return isRotation;
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        if (videoWidth * videoHeight == 0) {
            return;
        }
        if (videoWidth > videoHeight) {
            isRotation = true;
        } else {
            isRotation = false;
        }
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double videoRatio = (double) viewWidth / (double) viewHeight;//显示比例
        double aspectRatio = (double) videoWidth / (double) videoHeight;//视频比例
        int newWidth, newHeight;
        if (videoWidth > videoHeight) {//正常比例16：9
            if (videoRatio > aspectRatio) {//16:9>16:10
                newWidth = (int) (viewHeight * aspectRatio);
                newHeight = viewHeight;
            } else {//16:9<16:8
                newWidth = viewWidth;
                newHeight = (int) (viewWidth / aspectRatio);
            }
        } else {//非正常可能是 90度
            //16:9>1:9
            newWidth = (int) (viewHeight * aspectRatio);
            newHeight = viewHeight;
        }

        float xoff = (viewWidth - newWidth) / 2f;
        float yoff = 0;//(viewHeight - newHeight) / 2f;
        Matrix txform = new Matrix();
        getTransform(txform);
//
//
//        txform.setScale((float) newWidth / viewWidth, (float) newHeight
//                / viewHeight);
//
//        txform.postTranslate(xoff, yoff);
//        setTransform(txform);
//        Logger.i(tag, "video=" + videoWidth + "x" + videoHeight + " view="
//                + viewWidth + "x" + viewHeight + " newView=" + newWidth + "x"
//                + newHeight + " off=" + xoff + "," + yoff);

        float sx = (float) getWidth() / (float) mVideoWidth;
        float sy = (float) getHeight() / (float) mVideoHeight;

        Matrix matrix = new Matrix();

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getWidth() - mVideoWidth) / 2, (getHeight() - mVideoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(mVideoWidth / (float) getWidth(), mVideoHeight / (float) getHeight());

        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy){
            matrix.postScale(sy, sy, getWidth() / 2, getHeight() / 2);
        }else{
            matrix.postScale(sx, sx, getWidth() / 2, getHeight() / 2);
        }

        setTransform(matrix);
        postInvalidate();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            adjustAspectRatio(mVideoWidth, mVideoHeight);
        }
    }

    private int mVideoWidth, mVideoHeight;

    @Override
    public void onVideoSizeChanged(int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0) return;
        this.mVideoWidth = visibleWidth;
        this.mVideoHeight = visibleHeight;
        post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(mVideoWidth, mVideoHeight);
            }
        });
    }

}