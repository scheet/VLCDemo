package com.jwd.vlcplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcore.io.DiskLruCache;

/**
 * Created by ralfchen on 2016/10/20.
 */
public class VPaiPicasso {
    private static final String TAG = "VpaiPicasso";
    private static final int CACHE_MANAGER_VERSION = 11;

    static private Picasso mNetworkPicasso = null;
    //static private Picasso mLocalPicasso;


    public static Picasso networkPicasso(Context context) {
        if (mNetworkPicasso == null) {
            Picasso.Builder builder = new Picasso.Builder(context);
            builder = builder.memoryCache(new MemDiskCache(context));
            mNetworkPicasso = builder.build();
        }
        return mNetworkPicasso;
        // return Picasso.with(context);
    }

    public static Picasso localPicasso(Context context) {
        return Picasso.with(context);
    }

    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    static public final class MemDiskCache implements Cache {

        private DiskLruCache mDiskLruCache;
        private com.squareup.picasso.LruCache mMemoryCache;

        public MemDiskCache(Context context) {
            mMemoryCache = new LruCache(context);

            File cacheDir = getDiskCacheDir(context, "vpaithumbcache");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            Log.v(TAG, "dir is " + cacheDir.getAbsolutePath() + ", max size is " + mMemoryCache.maxSize());
            // 创建DiskLruCache实例，初始化缓存数据
            try {
                mDiskLruCache = DiskLruCache.open(cacheDir, CACHE_MANAGER_VERSION, 1, 10 * 1024 * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Bitmap get(String url) {
            Bitmap bmp = mMemoryCache.get(url);
            if (bmp != null && !bmp.isRecycled())
                return bmp;

            try {
                String key = hashKeyForDisk(url);
                // 查找key对应的缓存
                DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                if (snapShot == null) {
                    return null;
                }

                FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                FileDescriptor fileDescriptor = fileInputStream.getFD();
                if (fileDescriptor == null) {
                    mDiskLruCache.remove(key);
                    mDiskLruCache.flush();
                    return null;
                }


                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                if (bitmap == null) {
                    mDiskLruCache.remove(key);
                    mDiskLruCache.flush();
                    return null;
                }
                // 将Bitmap对象添加到内存缓存当中
                Log.v(TAG, "set new bitmap " + bitmap + " of url " + url);
                mMemoryCache.set(url, bitmap);
                return bitmap;

            } catch (IOException e) {
                Log.e(TAG, "Error: get() " + e.getMessage());
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Error: get() " + e.getMessage());
            }
            return null;
        }

        @Override
        public void set(String url, Bitmap bitmap) {
            if (url == null || bitmap == null) {
                return;
            }
            try {
                if (bitmap.isRecycled()) {
                    return;
                }

                mMemoryCache.set(url, bitmap);

                OutputStream outputStream;
                DiskLruCache.Editor editor = null;
                String key = hashKeyForDisk(url);

                editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    outputStream = editor.newOutputStream(0);

                    boolean sucess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    if (sucess)
                        editor.commit();
                    else
                        editor.abort();
                    editor = null;
                }
                mDiskLruCache.flush();
            } catch (Exception e) {
                Log.e(TAG, "Error: set() " + e.getMessage());
            }
        }

        @Override
        public int size() {
            return mMemoryCache.size();
        }

        @Override
        public int maxSize() {
            return mMemoryCache.maxSize();
        }

        @Override
        public void clear() {
            mMemoryCache.clear();
            //TODO: clear disk cache?
        }

        @Override
        public void clearKeyUri(String keyPrefix) {
            mMemoryCache.clearKeyUri(keyPrefix);
            //TODO: clearn disk cache?
        }

        /**
         * 使用MD5算法对传入的key进行加密并返回。
         */
        public String hashKeyForDisk(String key) {
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());

                cacheKey = bytesToHexString(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }

        private String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }
    }

    public static class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();//回收垃圾
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);//定义一个渲染器
            paint.setShader(shader);//设置渲染器
            paint.setAntiAlias(true);//设置抗拒齿，图片边缘相对清楚

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);//绘制图形

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

}
