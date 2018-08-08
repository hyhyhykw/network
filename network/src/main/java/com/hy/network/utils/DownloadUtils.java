package com.hy.network.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.hy.library.Base;
import com.hy.library.utils.FileUtils;
import com.hy.library.utils.Logger;
import com.hy.library.utils.ObjectsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created time : 2018/4/11 14:48.
 *
 * @author HY
 */
public class DownloadUtils {

    private final Context mContext;
    private final String url;
    private final String fileDir;
    private final String fileName;

    private DownloadUtils(Context activity, String url, String fileDir, String fileName) {
        mContext = activity;
        this.url = url;
        this.fileDir = fileDir;
        this.fileName = fileName;
    }

    public DownloadUtils(Builder builder) {
        this(builder.mContext, builder.url, builder.fileDir, builder.fileName);
    }

    private boolean isCancel = false;

    public void cancel() {
        isCancel = true;
    }

    public void download(@NonNull DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
        new DownloadTask(this).execute();
    }

    private DownloadListener mDownloadListener;

    private static class DownloadTask extends AsyncTask<String, Integer, File> {
        private WeakReference<DownloadUtils> mReference;

        DownloadTask(DownloadUtils downloadUtils) {
            mReference = new WeakReference<>(downloadUtils);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected File doInBackground(String... strings) {
            if (null == mReference) return null;
            DownloadUtils utils = mReference.get();
            if (utils == null) return null;
            File file = new File(utils.fileDir + File.separator + utils.fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            try {
                URL url = new URL(utils.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestMethod("POST");

                InputStream input = conn.getInputStream();
                int length = conn.getContentLength();
                utils.mDownloadListener.onStart(FileUtils.formatFileSize(length));

                int sum = 0;

                byte[] bytes = new byte[1024];
                FileOutputStream fos = new FileOutputStream(file);

                int len;
                while ((len = input.read(bytes)) != -1) {
                    if (utils.isCancel) {
                        return null;
                    }
                    fos.write(bytes, 0, len);
                    sum += len;
                    int progress = (int) (sum / (float) length * 100);
                    publishProgress(progress, sum, length);
                }

                fos.close();
                input.close();
                conn.disconnect();
                return file;
            } catch (MalformedURLException e) {
                Logger.e("下载URL有误", e);
            } catch (IOException e) {
                Logger.e("连接打开失败", e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (null == mReference) return;
            DownloadUtils utils = mReference.get();
            if (utils == null) return;
            Integer value = values[0];
            utils.mDownloadListener.onProgress(value);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (null == mReference) return;
            DownloadUtils utils = mReference.get();
            if (utils == null) return;
            if (null == file) {
                utils.mDownloadListener.onFailed();
            } else {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(utils.mContext, Base.getDelegate().getContext().getPackageName()+".file_provider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                utils.mDownloadListener.onSuccess(new FileEntry(file, uri));
            }
        }
    }

    public static final class Builder {
        private final Context mContext;
        private String url;
        private String fileDir;

        private String fileName;

        public Builder(Context activity) {
            mContext = activity;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder fileDir(String fileDir) {
            this.fileDir = fileDir;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public DownloadUtils build() {
            return new DownloadUtils(this);
        }
    }


    public interface DownloadListener {
        void onStart(String size);

        void onProgress(int progress);

        void onSuccess(FileEntry fileEntry);

        void onFailed();
    }


    public static final class FileEntry {
        private final File mFile;
        private final Uri mUri;
        private final String mimeType;


        FileEntry(File file, Uri uri) {
            mFile = file;
            mUri = uri;
            mimeType = FileUtils.getMime(mFile.getName());
        }

        public File getFile() {
            return mFile;
        }

        public Uri getUri() {
            return mUri;
        }

        public String getMimeType() {
            return mimeType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileEntry fileEntry = (FileEntry) o;
            return ObjectsUtils.equals(mFile, fileEntry.mFile) &&
                    ObjectsUtils.equals(mUri, fileEntry.mUri);
        }

        @Override
        public int hashCode() {
            return ObjectsUtils.hash(mFile, mUri);
        }
    }
}
