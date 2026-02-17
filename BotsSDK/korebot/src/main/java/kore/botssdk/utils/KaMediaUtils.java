package kore.botssdk.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kore.botssdk.exceptions.NoExternalStorageException;
import kore.botssdk.exceptions.NoWriteAccessException;
import kore.botssdk.models.KoreMedia;

/**
 * Created by Shiva Krishna on 4/3/2018.
 */

public class KaMediaUtils {
    public static final String MEDIA_APP_FOLDER = "Kore";
    public static final String DOWNLOADED_IMAGE_FOLDER = "Kore Image";
    public static final String DOWNLOADED_AUDIO_FOLDER = "Kore Audio";
    public static final String DOWNLOADED_VIDEO_FOLDER = "Kore Video";
    public static final String DOWNLOADED_DOCUMENT_FOLDER = "Kore Document";
    public static final String DOWNLOAD_ARCHIVE_FOLDER = "Kore Archive";
    private static final String LOG_TAG = "MediaUtil";
    static File mediaStorageDir = null;

    public static void setupAppDir(Context context, String type) {
        try {
            String path = context.getFilesDir() + File.separator + MEDIA_APP_FOLDER;
            if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_AUDIO))
                mediaStorageDir = new File(path, DOWNLOADED_AUDIO_FOLDER);
            else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_VIDEO))
                mediaStorageDir = new File(path, DOWNLOADED_VIDEO_FOLDER);
            else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_IMAGE))
                mediaStorageDir = new File(path, DOWNLOADED_IMAGE_FOLDER);
            else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_ARCHIVE)) {
                mediaStorageDir = new File(path, DOWNLOAD_ARCHIVE_FOLDER);
            } else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_DOCUMENT)) {
                mediaStorageDir = new File(path, DOWNLOADED_DOCUMENT_FOLDER);
            }

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Exception: at setupAppDir() "+e);
        }
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(String type, String fileName, String fileExt) throws NoExternalStorageException, NoWriteAccessException {
        // Create a media file name
        if (fileName != null && fileName.indexOf(".") > 0)
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(new Date()),
                appDirPath = getAppDir();
        File mediaFile = null;
        int attemptCount = 0;
        StringBuilder fileExtBuilder = new StringBuilder(fileExt);
        while (mediaFile == null || mediaFile.exists()) {
            String name = StringUtils.isNullOrEmptyWithTrim(fileName) ? timeStamp : fileName;
            if (attemptCount != 0) {
                name = name + attemptCount;
            }
            attemptCount++;

            if(!fileExtBuilder.toString().contains("."))
                fileExtBuilder.insert(0, ".");

            if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_AUDIO)) {
                mediaFile = new File(appDirPath + File.separator + name + fileExtBuilder);
            } else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_VIDEO)) {
                mediaFile = new File(appDirPath + File.separator + name + fileExtBuilder);
            } else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_IMAGE)) {
                mediaFile = new File(appDirPath + File.separator + name + fileExtBuilder);
            } else if (type.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_ARCHIVE)) {
                mediaFile = new File(appDirPath + File.separator + name + ".kore");    //".kore"
            } else {
                mediaFile = new File(appDirPath + File.separator + name + "." + type);    //".kore"
                //return null;
            }
        }
        return mediaFile;
    }

    public static String getAppDir() {
        return mediaStorageDir.getPath();
    }

    public static String saveFileToKoreWithStream(Context mContext, Uri uri, String fileName, String ext) {
        InputStream inputStream = null;
        OutputStream out = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            File file = KaMediaUtils.getOutputMediaFile(BitmapUtils.obtainMediaTypeOfExtn(ext), fileName, ext);
            inputStream = contentResolver.openInputStream(uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                out = Files.newOutputStream(file.toPath());
            } else out = new FileOutputStream(file);

            int read;
            byte[] bytes = new byte[1024];

            if (inputStream != null) {
                while ((read = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            LogUtils.e(LOG_TAG, "Exception: at saveFileToKoreWithStream() "+ e);
        } finally {
            try {
                //Closing output stream
                if (out != null) out.close();
                //Closing input stream
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                LogUtils.e(LOG_TAG, "Exception: at saveFileToKoreWithStream() "+ e);
            }
        }
        return null;
    }

    public static void saveFileToKorePath(String sourceFilePath, String destinationFilePath) {

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        FileInputStream fis = null;

        if (sourceFilePath == null)
            return;

        try {
            fis = new FileInputStream(sourceFilePath);
            bis = new BufferedInputStream(fis);
            fos = new FileOutputStream(destinationFilePath, false);
            bos = new BufferedOutputStream(fos);
            byte[] buf = new byte[1024];
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            bos.flush();
        } catch (IOException e) {
            LogUtils.e(LOG_TAG, "Exception: at saveFileToKorePath() "+ e);
        } finally {
            try {
                if (bos != null) bos.close();
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, "Exception: at saveFileToKorePath() "+ e);
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, "Exception: at saveFileToKorePath() "+ e);
            }
            try {
                if (bis != null) bis.close();
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, "Exception: at saveFileToKorePath() "+ e);
            }
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                LogUtils.e(LOG_TAG, "Exception: at saveFileToKorePath() "+ e);
            }
        }
    }

    public static void saveFileFromUrlToKorePath(Context context, String sourceFilePath) {
        new DownloadFileFromURL(context).execute(sourceFilePath);
    }

    public static String getMediaExtension(String MEDIA_TYPE) {
        String audio_ext;
        String video_ext;

        audio_ext = ".m4a";
        video_ext = ".mp4";
        if (MEDIA_TYPE.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_VIDEO)) {
            return video_ext;
        } else if (MEDIA_TYPE.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_IMAGE)) {
            return ".jpg";
        } else {
            return audio_ext;
        }
    }

    /**
     * Background Async Task to download file
     */
    static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private final Context context;

        public DownloadFileFromURL(Context context) {
            this.context = context;
        }

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ToastUtils.showToast(context, "Downloading...");
        }


        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            OutputStream output = null;
            InputStream input = null;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                // input stream to read file - with 8k buffer
                input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                String filePath = KaMediaUtils.getAppDir() + File.separator + StringUtils.getFileNameFromUrl(url.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    output = Files.newOutputStream(new File(filePath).toPath());
                } else output = new FileOutputStream(filePath);
                byte[] data = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();
            } catch (Exception e) {
                LogUtils.e("Error: ", "Exception in downloading from url"+ e);
            } finally {
                try {
                    //Closing output Stream
                    if (output != null) output.close();
                    //Closing input stream
                    if (input != null) input.close();
                } catch (Exception e) {
                    LogUtils.e("Error: ", "Exception in downloading from url"+ e);
                }
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            ToastUtils.showToast(context, "Downloading completed");
        }
    }
}