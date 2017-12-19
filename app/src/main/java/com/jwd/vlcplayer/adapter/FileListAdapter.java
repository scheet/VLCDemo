package com.jwd.vlcplayer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.jwd.vlcplayer.R;
import com.jwd.vlcplayer.utils.FileInfo;
import com.jwd.vlcplayer.utils.ThumbnailCacheManager;
import com.jwd.vlcplayer.utils.VPaiPicasso;
import com.jwd.vlcplayer.view.HeadListView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by scheet on 2017/10/26.
 */

public class FileListAdapter extends BaseAdapter
        implements ThumbnailCacheManager.ThumbnailCacheListener,HeadListView.HeaderAdapter,SectionIndexer {
    public final static String TAG = "FileListAdapter" ;

    private List<FileInfo> mFileInfos = null;
    Activity activity;

    private LayoutInflater mInflater;
    private int mGridItemHeight,screenWidth;


    /* 是不是城市频道，  true：是   false :不是*/
    public boolean isCityChannel = false;

    /* 是不是第一个ITEM，  true：是   false :不是*/
    public boolean isfirst = true;
    private List<Integer> mPositions;
    private List<String> mSections;

    public FileListAdapter(Activity activity, List<FileInfo> fileInfos) {
        this.activity = activity;
        mInflater = LayoutInflater.from(activity);
        mFileInfos = fileInfos;
        screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();

        mGridItemHeight = screenWidth/2 + dip2px(20);
//        Log.e(TAG, "＝＝＝＝＝　FileListAdapter　screenWidth = "+screenWidth);
//        Log.e(TAG, "＝＝＝＝＝　FileListAdapter　mGridItemHeight = "+mGridItemHeight);

    }


    @Override
    public int getCount() {
        return (mFileInfos == null ? 0 :mFileInfos.size());
    }

    @Override
    public Object getItem(int i) {
        return (mFileInfos == null ? null : mFileInfos.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(int position, View convertView, ViewGroup par) {
//        Log.e(TAG, "＝＝＝＝FileListAdapter getView ＝＝＝＝");
        ViewHolder holder;
        FileInfo finfo = mFileInfos.get(position);
//        Log.e(TAG, "getView fileInfos = " + finfo);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_item, null);
            convertView.setLayoutParams(new GridView.LayoutParams(
                    GridView.LayoutParams.MATCH_PARENT, mGridItemHeight));

            holder = new ViewHolder();
            holder.container = convertView.findViewById(R.id.item_container);
            holder.videoName = (TextView) convertView.findViewById(R.id.vide_name);
            holder.videoCover = ((ImageView) convertView.findViewById(R.id.video_cover));

            holder.authorPhoto = (ImageView) convertView.findViewById(R.id.author_photo);
            holder.authorName = ((TextView) convertView.findViewById(R.id.author_name));
            holder.fileId = ((TextView) convertView.findViewById(R.id.file_id));
//            holder.category = ((TextView) convertView.findViewById(R.id.category));
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fileId.setText(finfo.fileId);
//        holder.category.setText(finfo.category);

        //url cover and title
        String cover = finfo.cover;
        Log.e(TAG,"getView cover = " +cover);
        VPaiPicasso.networkPicasso(activity)
                .load(cover)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.download_loading)
                .error(R.drawable.download_loading)
                .into(holder.videoCover);
        holder.videoName.setText(finfo.videoTitle);

        //author information
        holder.authorName.setText(finfo.authorName);
        String authorPhoto = finfo.authorPhoto;
        Log.e(TAG,"authorPhoto = " +authorPhoto);
        VPaiPicasso.networkPicasso(activity)
                .load(authorPhoto)
                .resize(120, 120)
                .centerCrop()
                .placeholder(R.drawable.download_loading)
                .error(R.drawable.download_loading)
                .into(holder.authorPhoto);


        return convertView;
    }


    @Override
    public void onThumbnailCacheDone(String url, String key, int type, Bitmap bitmap) {

    }

    private class ViewHolder {
        View       container;
        TextView   videoName;
        ImageView  videoCover;
        ImageView  authorPhoto;
        TextView  authorName;
        TextView  fileId;
//        TextView  category;

    }



    /*
     * 设置是不是特殊的频道（城市频道）
     */
    public void setCityChannel(boolean iscity){
        isCityChannel = iscity;
    }


    private int dip2px(float dpValue) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }




    @Override
    public int getHeaderState(int position) {
        // TODO Auto-generated method stub
        int realPosition = position;
        if(isCityChannel){
            if(isfirst){
                return HEADER_GONE;
            }
        }
        if (realPosition < 0 || position >= getCount()) {
            return HEADER_GONE;
        }
        int section = getSectionForPosition(realPosition);
        int nextSectionPosition = getPositionForSection(section + 1);
        if (nextSectionPosition != -1
                && realPosition == nextSectionPosition - 1) {
            return HEADER_PUSHED_UP;
        }
        return HEADER_VISIBLE;
    }
    @Override
    public void configureHeader(View header, int position, int alpha) {
        int realPosition = position;
        int section = getSectionForPosition(realPosition);
        String title = (String) getSections()[section];
        ((TextView) header.findViewById(R.id.section_text)).setText(title);
        ((TextView) header.findViewById(R.id.section_day)).setText("今天");
    }
    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= getCount()) {
            return -1;
        }
        int index = Arrays.binarySearch(mPositions.toArray(), position);
        return index >= 0 ? index : -index - 2;
    }
    @Override
    public Object[] getSections() {
        // TODO Auto-generated method stub
        return mSections.toArray();
    }
    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= mPositions.size()) {
            return -1;
        }
        return mPositions.get(sectionIndex);
    }
}
