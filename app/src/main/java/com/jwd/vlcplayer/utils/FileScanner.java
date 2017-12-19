package com.jwd.vlcplayer.utils;

import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jwd.vlcplayer.utils.util.FileMediaType;
import com.jwd.vlcplayer.utils.util.Match4Req;
import com.jwd.vlcplayer.utils.util.WorkReq;
import com.jwd.vlcplayer.utils.util.WorkThread;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by scheet on 2017/10/26.
 */

public class FileScanner {

    private Handler mHandler;
    private WorkThread mScannerThread;
    private FileScannerReq mLastScannerReq;
    public static final int RESULT_TYPE_SCANNER = 1;
    public static final int RESULT_TYPE_SORT = 3;

    public FileScanner() {
        mHandler = new Handler();
    }

    private final static String TAG = "FileScanner";

    public static ArrayList<FileInfo> getJSONArray(JSONArray array, boolean needUp) {
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();

        Log.d(TAG, "readJSONArray() array= " + array.toString());
        try {
            for (int i = 0; i < array.size(); i++) {
                JSONObject jso = array.getJSONObject(i);
                FileInfo info = new FileInfo();
                info.videoTitle = jso.getString("FileTitle");
                info.cover = jso.getString("Cover");
                info.videoUrl = jso.getString("FilePath");
                info.authorName = jso.getString("AuthorName");
                info.authorPhoto = jso.getString("AuthorPhoto");
                info.fileId = jso.getString("Id");
//                info.category = jso.getString("Category");
                list.add(info);

            }
        } catch (JSONException e) {
            Log.i(TAG, "JSONException = " + e);
        }
        return list;
    }

    /**
     * should override this to get the result.
     * Invoked in UI thread.
     *
     * @param scanPath
     * @param fileList
     */
    public void onResult(final int type, final String scanPath, final ArrayList<FileInfo> fileList) {
    }

    public void startScanner(boolean needUp) {
        startScanner(FileMediaType.ALL_TYPES, needUp);
    }

    //因为只需要扫描最后一个，那么不需要队列来存储path
    public void startScanner(int listType, boolean needUp) {


        if (null == mScannerThread) {
            mScannerThread = new WorkThread();
            mScannerThread.start();
        }

        FileScannerReq fScannerReq = new FileScannerReq(mHandler, listType, needUp);
        if (!mScannerThread.isDuplicateWorking(fScannerReq)) {
            mScannerThread.cancelReqsList();
            mScannerThread.addReq(fScannerReq);

            //save the last scanner req
            mLastScannerReq = fScannerReq;
        }
    }

    private class FileScannerReq implements WorkReq, Match4Req {

        public String mScanPath;
        public Handler mHandler;
        public int mListType;
        private boolean mCancel = false;
        private boolean mNeedUp = false;

        public FileScannerReq(Handler handler, int listType, boolean needUp) {
//            mScanPath = path;
            mHandler = handler;
            mListType = listType;
            mNeedUp = needUp;
        }

        @Override
        public boolean matchs(WorkReq req) {
            if (req instanceof FileScannerReq) {
                FileScannerReq req2 = (FileScannerReq) req;
                if (mScanPath.equals(req2.mScanPath) && mListType == req2.mListType) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void execute() {
//            final ArrayList<FileInfo> list = guardRun();
            //always report scanDone even this folder is not readable or error.

            if (!mCancel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLastScannerReq == FileScannerReq.this) {
//                            if (!mCancel) {
//                                if (list == null)
//                                    onResult(RESULT_TYPE_SCANNER, mScanPath, new ArrayList<FileInfo>());
//                                else
//                                    onResult(RESULT_TYPE_SCANNER, mScanPath, list);
//                            }
                            mLastScannerReq = null;
                        }
                    }
                });
            }
        }

        @Override
        public void cancel() {
            mCancel = true;
        }

        public ArrayList<FileInfo> guardRun() {
            File fDest = new File(mScanPath);
            if (!fDest.isDirectory() || !fDest.exists())
                return null;

            File[] files = fDest.listFiles();
            if (files == null)
                return null;

            final ArrayList<FileInfo> folderList = new ArrayList<FileInfo>();
            boolean isAddDir = isNeedDir();
            String name;
            if (isAddDir) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isDirectory()) {
                        name = f.getName();
                        if ((name.length() > 0) && !name.startsWith(".")
                                && f.canRead()) {
//                            FileInfo fi = new FileInfo(name, true);
//                            fi.modifytime = f.lastModified();
//                            fi.isDirectory = true;
//                            fi.lsize = 0;
//                            fi.sub = f.listFiles().length;
//                            folderList.add(fi);
                        }
                    }
                    if (mCancel) {
                        Log.d(TAG, "scan abort1: " + i + "/" + files.length);
                        return null;
                    }
                }
            }

//            if (mNeedUp) {
//                FileInfo up = new FileInfo("..", true);
//                up.modifytime = System.currentTimeMillis();
//                up.isDirectory = true;
//                up.lsize = 0;
//                folderList.add(0, up);
//            }

//            String filePath = mScanPath + "/";
//            for (int i = 0; i < folderList.size(); i++) {
//                folderList.get(i).path = filePath;
//                folderList.get(i).size = "";
//            }


            LinkedList<FileInfo> fileList = new LinkedList<FileInfo>();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.isDirectory()) {
                    name = f.getName();
//                    if ((name.length() > 0) && (!name.startsWith(".")) && isMatchListType(name)) {
//                        FileInfo fi = new FileInfo(name, false);
//                        fi.size = fileSizeMsg(f.getPath());
//                        fi.lsize = f.length();
//                        fi.modifytime = name2Date(name);
//                        if (fi.modifytime == 0)
//                            fi.modifytime = f.lastModified();
//                        fi.fileType = FileMediaType.getMediaType(name);
//                        fi.isDirectory = false;
//                        if (fi.fileType == FileMediaType.IMAGE_TYPE ||
//                                fi.fileType == FileMediaType.VIDEO_TYPE)
//                            fileList.add(fi);
//                    }
                }
                if (mCancel) {
                    Log.d(TAG, "scan abort2: " + i + "/" + files.length);
                    return null;
                }
            }

//            for (int i = 0; i < fileList.size(); i++) {
//                fileList.get(i).path = filePath;
//            }

            folderList.addAll(fileList);    //now folder list contains folder and file.
            return folderList;
        }

        private boolean isNeedDir() {
            return false;
        }

        private boolean isMatchListType(String fname) {
            if (mListType == FileMediaType.ALL_TYPES) {
                return true;
            }

            int type = FileMediaType.getMediaType(fname);
            return (mListType & type) == type;
        }
    }

    public void generateId(final List<FileInfo> fileList) {
        if (fileList == null) {
            return;
        }
        if (null == mScannerThread) {
            mScannerThread = new WorkThread();
            mScannerThread.start();
        }
        HeaderIdReq ftagReq = new HeaderIdReq(fileList);
        if (!mScannerThread.isDuplicateWorking(ftagReq)) {
            mScannerThread.addReq(ftagReq);
        }
    }


    private class HeaderIdReq implements WorkReq, Match4Req {
        public List<FileInfo> mFileList;
        //public List<FileTag> mFileTagList;

        //private String mFilePath;
        public boolean mCancel = false;

        public HeaderIdReq(List<FileInfo> fileList) {
            mFileList = fileList;
        }

        @Override
        public boolean matchs(WorkReq req) {

            return false;
        }

        @Override
        public void execute() {



            if (!mCancel) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onResultList(mFileList);
                    }
                });
            }
        }

        @Override
        public void cancel() {
            mCancel = true;
        }

    }


    /**
     * should override this to get the result.
     * Invoked in UI thread.
     */
    public void onResultList(final List<FileInfo> fileList) {

    }

}
