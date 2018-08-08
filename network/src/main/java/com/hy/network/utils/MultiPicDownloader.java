package com.hy.network.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.hy.library.Base;
import com.hy.library.base.WeakHandler;
import com.hy.library.utils.FileUtils;
import com.hy.library.utils.Logger;
import com.hy.library.utils.ObjectsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created time : 2018/4/16 17:07.
 *
 * @author HY
 */
public final class MultiPicDownloader {
    /**
     * 线程池，用于存放下载图片的任务
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /* HttpClient */
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    private static final int TASK_FINISH = 200;
    private static final int TASK_FAILED = 404;
    /**
     * 下载成功的数量
     */
    private int count = 0;

    /**
     * 下载失败的数量
     */
    private int failedCount = 0;

    /**
     * 所有的下载地址
     */
    private final ArrayList<String> urls = new ArrayList<>();
    /**
     * 下载完成的图片对象集合
     */
    private final TreeSet<ImageInfo> data = new TreeSet<>();
    private final Context mContext;

    private static final String IMAGE_PATH = "movie-image";
    private final String picDir;

    private final ArrayList<String> failed_tags = new ArrayList<>();
    private final ArrayList<String> success_tags = new ArrayList<>();
    private final DownHandler mHandler;

    //    val file = getDiskCacheDir(md5(url)!!, getExtension(url))
    public MultiPicDownloader(Context context) {
        this(context, IMAGE_PATH);
    }

    /**
     * @param context 上下文
     * @param picDir  存储图片的文件夹
     */
    public MultiPicDownloader(@NonNull Context context, @NonNull String picDir) {
        mContext = context;
        this.picDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "Android" + File.separator + "data" + File.separator + context.getPackageName() + File.separator + "caches" + File.separator + picDir + File.separator;
        mHandler = new DownHandler(this);
    }

    public void download(@NonNull ArrayList<String> urls) {
        this.urls.clear();
        this.urls.addAll(urls);
        this.failed_tags.clear();
        this.success_tags.clear();
        count = 0;
        for (String url : urls) {
            executor.execute(new DownTask(url));
        }
    }

    public void download(String... url) {
        download(new ArrayList<>(Arrays.asList(url)));
    }

    /**
     * 根据图片url创建一个新的下载任务，如果该图片已经下载过，就直接加入图片集合中，并且发送下载成功的消息
     */
    private final class DownTask implements Runnable {
        private final String tag;
        private final String url;
        private final File file;
        private final String extension;

        DownTask(@NonNull String url) {
            this.url = url;
            tag = FileUtils.md5(url);
            extension = FileUtils.getExtension(url);
            file = new File(picDir + tag + "." + extension);
        }

        @NonNull
        final String getTag() {
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DownTask downTask = (DownTask) o;
            return ObjectsUtils.equals(getTag(), downTask.getTag());
        }

        @Override
        public int hashCode() {
            return ObjectsUtils.hash(getTag());
        }

        @Override
        public void run() {
            if (file.exists()) {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mContext, Base.getDelegate().getContext().getPackageName()+".file_provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                Logger.e(uri);
                data.add(new ImageInfo(file, uri));

                Message msg = Message.obtain();
                msg.what = TASK_FINISH;
                msg.obj = url;
                mHandler.sendMessage(msg);
                if (null != mOnDownloadListener)
                    mOnDownloadListener.onDownload(DownStatus.SUCCESS, url, 100);
            } else {
                boolean mkdirs = file.getParentFile().mkdirs();
                Logger.d("文件夹创建" + (mkdirs ? "成功" : "失败"));
                Request request = new Request.Builder()
                        .post(new FormBody.Builder().build())
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Message msg = Message.obtain();
                        msg.what = TASK_FAILED;
                        msg.obj = url;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        ResponseBody body = response.body();

                        if (null == body) {
                            Message msg = Message.obtain();
                            msg.what = TASK_FAILED;
                            msg.obj = url;
                            mHandler.sendMessage(msg);
                            return;
                        }
                        long length = body.contentLength();
                        //todo
                        InputStream ip = body.byteStream();

                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] bytes = new byte[1024];
                        int sum = 0;
                        int len;
                        while ((len = ip.read(bytes)) != -1) {
                            fos.write(bytes, 0, len);
                            sum += len;
                            int progress = (int) (sum * 100f / length);
                            if (null != mOnDownloadListener)
                                mOnDownloadListener.onDownload(DownStatus.DOWNLOADING, url, progress);
                        }

                        fos.flush();
                        fos.close();
                        ip.close();

                        Uri uri;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(mContext, Base.getDelegate().getContext().getPackageName()+".file_provider", file);
                        } else {
                            uri = Uri.fromFile(file);
                        }
                        Logger.d(uri);
                        data.add(new ImageInfo(file, uri));

                        Message msg = Message.obtain();
                        msg.what = TASK_FINISH;
                        msg.obj = url;
                        mHandler.sendMessage(msg);
                        if (null != mOnDownloadListener)
                            mOnDownloadListener.onDownload(DownStatus.SUCCESS, url, 100);
                    }
                });

            }
        }
    }

    /**
     * 中文：图片信息对象，其中有图片类型，图片文件对象，图片的Uri
     *
     * @author HY
     */
    public final class ImageInfo implements Comparable<ImageInfo> {

        private final File file;
        private final Uri imgUri;

        /**
         * @param file   图片文件 picture file
         * @param imgUri 图片Uri picture uri
         */
        ImageInfo(@NonNull File file, @NonNull Uri imgUri) {
            this.file = file;
            this.imgUri = imgUri;
        }

        @NonNull
        public final File getFile() {
            return file;
        }

        @NonNull
        public final Uri getImgUri() {
            return imgUri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageInfo imageInfo = (ImageInfo) o;
            return ObjectsUtils.equals(file, imageInfo.file) &&
                    ObjectsUtils.equals(imgUri, imageInfo.imgUri);
        }

        @Override
        public int hashCode() {

            return ObjectsUtils.hash(file, imgUri);
        }


        @Override
        public String toString() {
            return "ImageInfo{" +
                    "file=" + file +
                    ", imgUri=" + imgUri +
                    '}';
        }

        @Override
        public int compareTo(@NonNull ImageInfo o) {
            return Integer.compare(hashCode(), o.hashCode());
        }
    }

    /**
     * 中文：与主线程通信的Handler,为了防止内存泄漏，放在companion object中，相当于java中的static class
     */
    private static final class DownHandler extends WeakHandler<MultiPicDownloader> {

        DownHandler(@NonNull MultiPicDownloader multiPicDownloader) {
            super(multiPicDownloader);
        }

        @Override
        protected void handleMessage(Message msg, @NonNull MultiPicDownloader downloader) {
            String obj = (String) msg.obj;
            if (msg.what == TASK_FINISH) {
                downloader.count++;
                downloader.success_tags.add(obj);
            }
            if (msg.what == TASK_FAILED) {
                downloader.failedCount++;
                downloader.failed_tags.add(obj);
            }

            if (downloader.isFailed()) {
                downloader.stop();
                if (null != downloader.mOnDownloadListener)
                    downloader.mOnDownloadListener.onFailed();
            } else if (downloader.isFinish()) {
                downloader.stop();

                ArrayList<ImageInfo> list = new ArrayList<>(downloader.data);

                if (null != downloader.mOnDownloadListener) {
                    downloader.mOnDownloadListener.onComplete(downloader.failedCount, list);
                    downloader.mOnDownloadListener.onResult(downloader.failed_tags, downloader.success_tags);
                }
            }
        }
    }

    /**
     * 停止下载
     * stop download
     */
    private void stop() {
        executor.shutdown();
    }

    private boolean isFinish() {
        return urls.size() == count + failedCount;
    }

    private boolean isFailed() {
        return urls.size() == failedCount;
    }

    private OnDownloadListener mOnDownloadListener;

    public MultiPicDownloader setOnDownloadListener(OnDownloadListener onDownloadListener) {
        mOnDownloadListener = onDownloadListener;
        return this;
    }

    public interface OnDownloadListener {
        void onDownload(@NonNull DownStatus status, @NonNull String tag, int progress);

        void onFailed();

        void onComplete(int failedCount, @NonNull ArrayList<ImageInfo> imageInfos);

        void onResult(@NonNull ArrayList<String> failed, @NonNull ArrayList<String> succeed);
    }

    public enum DownStatus {
        DOWNLOADING,
        SUCCESS,
        FAILED
    }
}
