package com.jwd.vlcplayer.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jwd.vlcplayer.R;


/**
 * Created by EstarWu on 2017/8/29.
 */

public class VLCDialog {

    public static class Builder extends AlertDialog.Builder {
        private AlertDialog mAlertDialog;
        private LayoutInflater mInflater;
        private View mView, mTitleView, mContentView;
        private LinearLayout mCustomContentView;
        private TextView mTitleTextView, mContentTextView;

        public Builder(Context context) {
            this(context, AlertDialog.THEME_HOLO_DARK);
        }

        public Builder(Context context, int themeResId) {
            super(context, themeResId);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = mInflater.inflate(R.layout.custom_alert_dialog, null);
            mTitleView = mView.findViewById(R.id.alert_title_panel);
            mTitleTextView = (TextView) mView.findViewById(R.id.alert_title_text);
            mContentView = mView.findViewById(R.id.alert_content_panel);
            mContentTextView = (TextView) mView.findViewById(R.id.alert_content_text);
            mCustomContentView = (LinearLayout) mView.findViewById(R.id.alert_custom_panel);
        }

        public AlertDialog getDialog() {
            return mAlertDialog;
        }

        @Override
        public AlertDialog.Builder setView(View view) {
            mContentView.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mCustomContentView.addView(view, lp);
            return this;
//            return super.setView(view);
        }

        @Override
        public AlertDialog.Builder setView(int layoutResId) {
            View view = mInflater.inflate(layoutResId, null);
            return setView(view);
//            return super.setView(layoutResId);
        }

        @Override
        public AlertDialog create() {
            super.setView(mView);
            mAlertDialog = super.create();
            return mAlertDialog;
        }

        @Override
        public AlertDialog.Builder setTitle(@StringRes int titleId) {
//        return super.setTitle(titleId);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleTextView.setText(titleId);
            return this;
        }

        @Override
        public AlertDialog.Builder setTitle(CharSequence title) {
//        return super.setTitle(title);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleTextView.setText(title);
            return this;
        }

        @Override
        public AlertDialog.Builder setMessage(@StringRes int messageId) {
//        return super.setMessage(messageId);
            setMessage(getContext().getResources().getString(messageId));
            return this;
        }

        @Override
        public AlertDialog.Builder setMessage(CharSequence message) {
//        return super.setMessage(message);
            mContentTextView.setText(message);
            return this;
        }
    }

}
