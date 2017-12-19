package com.vlcplayer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.log.Logger;
import com.vlcplayer.utils.BitmapUtil;
import com.vlcplayer.utils.StringUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import com.vlcplayer.utils.ShowToast;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Author：caoyamin
 * Time: 2017/1/22
 */

public class VlcControllerView extends FrameLayout {

    private static final String TAG = "VideoControllerView";

    private MediaPlayerControl mPlayer;
    private Context mContext;
    //private View mAnchor;
    private View controller;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing = false;
    private boolean isFullscreen = false;
    private boolean mDragging;
    private static final int sDefaultTimeout = 10000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton;

    private ImageView mNextButton;
    private ImageView mPrevButton;
    private ImageView mFullscreenButton;
    private ImageView mSnapshot;
    private Handler mHandler = new MessageHandler(this);
    public View mRoot;
    private boolean isSnapshot = false;

    int init = 0;

    public VlcControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mRoot = this;
        Log.i(TAG, TAG);
    }

    public VlcControllerView(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        Log.i(TAG, TAG);
    }

    public VlcControllerView(Context context) {
        this(context, true);

        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (controller != null)
            initControllerView(controller);
    }

    public void setMediaPlayerListener(MediaPlayerControl player) {
        mPlayer = player;

        updatePausePlay();
        updateFullScreen();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view
     */
    public void setControllerView(VlcVideoView videoView) {


        Logger.e(videoView.getWidth() + videoView.getHeight() + "");
        controller = makeControllerView();
        LayoutParams bottom = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        addView(controller, bottom);

        controller.setVisibility(GONE);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        controller = inflate.inflate(R.layout.layout_controller, null);
        initControllerView(controller);
        return controller;
    }

    private void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.mediacontroller_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageView) v.findViewById(R.id.mediacontroller_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }


        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = (ImageView) v.findViewById(R.id.mediacontroller_next);

        if (mNextButton != null) {
            mNextButton.requestFocus();
            mNextButton.setOnClickListener(mNextListener);
        }

        mPrevButton = (ImageView) v.findViewById(R.id.mediacontroller_prev);
        if (mPrevButton != null) {
            mPrevButton.requestFocus();
            mPrevButton.setOnClickListener(mPrevListener);
        }


        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.mediacontroller_time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mSnapshot = (ImageView) v.findViewById(R.id.mediacontroller_snapshot);
        if (mSnapshot != null) {
            mSnapshot.requestFocus();
            mSnapshot.setOnClickListener(mSnapshotListener);
        }
    }


    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            /*if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }*/
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            if (mPrevButton != null) {
                mPrevButton.requestFocus();
            }
            if (mNextButton != null) {
                mNextButton.requestFocus();
            }
            disableUnsupportedButtons();

            controller.setVisibility(VISIBLE);
            mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        try {
            controller.setVisibility(GONE);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
                Log.e(TAG,"aaaaaa11111111111111111111111111111111");
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

//        Log.e(TAG, "==== 1111 === position= " + position);
//        Log.e(TAG, "==== 2222 === duration = " + duration);

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        Log.e(TAG, "====aaaaaaaaaaaaa=== position= " + position);

        Log.e(TAG, "====333333=== stringForTime(duration)= " + stringForTime(duration));
        Log.e(TAG, "====444444=== stringForTime(position)= " + stringForTime(position));


//        if(0 == position){
//            init++;
//            Log.e(TAG, "====oooooooooo=== init= " + init);
//        }
//
//        if(init > 2){
//          mPauseButton.setImageResource(R.drawable.mediacontroller_play);
//          mPlayer.pause();
//          init = 1;
//       }
//        if (stringForTime(position).equals(stringForTime(duration))
//                || position == duration) {
//            mPauseButton.setImageResource(R.drawable.mediacontroller_play);
//            mPlayer.pause();
//        }

        return position;
    }

    //private int size;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }


    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };


    private OnClickListener mFullscreenListener = new OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mSnapshotListener = new OnClickListener() {
        public void onClick(View v) {
            doToggleSnapshot();
        }
    };

    public void updatePausePlay() {
        if (controller == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.mediacontroller_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.mediacontroller_play);
        }
        invalidate();

    }

    public void updateFullScreen() {
        if (controller == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }

        if (isFullscreen) {
            mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_fullscreen_white_24dp);
        }
        //invalidate();
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }
        isFullscreen = !isFullscreen;
        mPlayer.toggleFullScreen(isFullscreen);
    }

    private void doToggleSnapshot() {
        if (mPlayer == null) {
            return;
        }
        isSnapshot = true;
        mPlayer.toggleSnapshot(isSnapshot);
        isSnapshot = false;
    }


    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;

            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newposition));
//            if (null != mPlayer) {
//                if (stringForTime((int) newposition).equals(stringForTime((int) duration))
//                        || newposition == duration) {
//                    mPauseButton.setImageResource(R.drawable.mediacontroller_play);
//                    mPlayer.pause();
//                }
//            }
//            if(0 == newposition){
//                init++;
//                Log.e(TAG, "====ppppppppppppp=== init= " + init);
//            }
//            if(init > 2){
//                mPauseButton.setImageResource(R.drawable.mediacontroller_play);
//                mPlayer.pause();
//                init = 1;
//            }

        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            Log.e(TAG,"aaaaaa22222222222222222222222222");
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private OnClickListener mPrevListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();
            Log.e(TAG,"aaaaaa33333333333333333333333");
            init = 0;
            show(sDefaultTimeout);
        }
    };

    private OnClickListener mNextListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos += 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();
            Log.e(TAG,"aaaaaa4444444444444444444");
            init = 0;
            show(sDefaultTimeout);
        }
    };

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();


        void toggleFullScreen(boolean fullscreen);

        void toggleSnapshot(boolean Snapshot);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VlcControllerView> mView;

        MessageHandler(VlcControllerView view) {
            mView = new WeakReference<VlcControllerView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VlcControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    Log.e(TAG,"aaaaaa555555555555555555555");
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}