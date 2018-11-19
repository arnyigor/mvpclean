package com.arny.mvpclean.data.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.ResponseBody;

public class FileUtils {
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final int FILE_TYPE_IMAGE = 0;
    public static final int FILE_TYPE_VIDEO = 1;
    public static final int FILE_TYPE_AUDIO = 2;
    public static final int FILE_TYPE_DOCUMENT = 3;
    private static final String DOCUMENT_SEPARATOR = ":";
    private static final String FOLDER_SEPARATOR = File.separator;

    public static File getOutputZipFile(String workDir, String name) {
        File dir = new File(workDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return new File(dir.getPath() + File.separator + name);
    }

    public static void zip(File[] files, String zipFile) throws IOException {
        final int BUFFER_SIZE = 10 * 1024;
        BufferedInputStream origin = null;
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            byte data[] = new byte[BUFFER_SIZE];
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                FileInputStream fi = new FileInputStream(absolutePath);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(absolutePath.substring(absolutePath.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        }
    }

    /**
     * Чтение файлов assets,в папке
     *
     * @param folder
     * @return
     */
    public static ArrayList<String> listAssetFiles(Context context, String folder) {
        ArrayList<String> fileNames = new ArrayList<>();
        if (context == null) {
            return null;
        }
        try {
            String[] files = context.getAssets().list(folder);
            Collections.addAll(fileNames, files);
            return fileNames;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean zipFileAtPath(String sourcePath, String zipFileName) {
        File dir = new File(sourcePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }
        File zipFile = new File(dir.getPath() + File.separator + zipFileName);
        try {
            zipFile.createNewFile();
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            int BUFFER = 2048;
            byte data[] = new byte[BUFFER];
            File srcFolder = new File(sourcePath);
            File[] files = srcFolder.listFiles();
            Log.d("zipFileAtPath", "Zip directory: " + srcFolder.getName());
            for (File file : files) {
                Log.d("zipFileAtPath", "Adding file: " + file.getName());
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.getName());
                Log.d(FileUtils.class.getSimpleName(), "zipFileAtPath: entry:" + entry);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            return true;
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
            return false;
        }
    }

    public static void refreshMedia(final Context context, int fileType, String filePath) {
        Uri uri;
        switch (fileType) {
            case FILE_TYPE_IMAGE:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case FILE_TYPE_VIDEO:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case FILE_TYPE_AUDIO:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case FILE_TYPE_DOCUMENT:
                uri = MediaStore.Files.getContentUri("external");
                break;
            default:
                uri = MediaStore.Files.getContentUri("external");
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(FileUtils.class.getSimpleName(), "onScanCompleted: path = " + path);
                        Log.d(FileUtils.class.getSimpleName(), "onScanCompleted: uri = " + uri);
                    }
                });
    }

    public static void removeFileFromMedia(Context context, String path) {
        context.getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{path});
        refreshGalleryOnDeleteFile(context, path);
    }

    public static void removeImagesFromMedia(Context context, String path) {
        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
        refreshGalleryOnDeleteFile(context, path);
    }

    private static void refreshGalleryOnDeleteFile(Context context, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(getFileUri(context, new File(path)));
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, getFileUri(context, new File(path))));
        }
    }

    public static void removeVideosFromMedia(Context context, String path) {
        context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "=?", new String[]{path});
        refreshGalleryOnDeleteFile(context, path);
    }

    public static void removeAudiosFromMedia(Context context, String path) {
        context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "=?", new String[]{path});
        refreshGalleryOnDeleteFile(context, path);
    }

    public static void galleryRefresh(Context context) {

        context.sendBroadcast(
                new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                )
        );
//
//		try {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//				Uri contentUri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
//				Log.d(FileUtils.class.getSimpleName(), "galleryRefresh: contentUri = " + contentUri);
//				mediaScanIntent.setData(contentUri);
//				context.sendBroadcast(mediaScanIntent);
//			} else {
//				context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    public static Bitmap getThumbnail(int id, Context context) {
        final String thumb_DATA = MediaStore.Images.Thumbnails.DATA;
        final String thumb_IMAGE_ID = MediaStore.Images.Thumbnails.IMAGE_ID;
        String[] projection = {
                thumb_DATA,
                thumb_IMAGE_ID};
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
        Bitmap thumbBitmap = null;
        if (cursor != null && cursor.moveToFirst()) {
            int thCulumnIndex = cursor.getColumnIndex(thumb_DATA);
            String thumbPath = cursor.getString(thCulumnIndex);
            thumbBitmap = BitmapFactory.decodeFile(thumbPath);
            cursor.close();
        }
        return thumbBitmap;
    }

    public static void removeNthLine(String f, int toRemove) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        // Leave the n first lines unchanged.
        for (int i = 0; i < toRemove; i++)
            raf.readLine();
        // Shift remaining lines upwards.
        long writePos = raf.getFilePointer();
        raf.readLine();
        long readPos = raf.getFilePointer();

        byte[] buf = new byte[1024];
        int n;
        while (-1 != (n = raf.read(buf))) {
            raf.seek(writePos);
            raf.write(buf, 0, n);
            readPos += n;
            writePos += n;
            raf.seek(readPos);
        }

        raf.setLength(writePos);
        raf.close();
    }

    public static String getDataDir(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getWorkDir(Context context) {
        try {
            return new File(context.getExternalFilesDir(null), "").getAbsolutePath();
//			return context.getFilesDir().getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void downloadUsingStream(String urlStr, String file) throws IOException {
        BufferedInputStream bis;
        FileOutputStream fis;
        try {
            URL url = new URL(urlStr);
            bis = new BufferedInputStream(url.openStream());
            fis = new FileOutputStream(file);
            byte[] buffer = new byte[10485760];
            int count;
            while ((count = bis.read(buffer, 0, 10485760)) != -1) {
                fis.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static long getUrlFileModified(String urlPath) {
        try {
            URL url = new URL(urlPath);
            URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            return uc.getLastModified();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void unzipGZ(String sourcePath, String destinationPath) throws IOException, DataFormatException {
        //Allocate resources.
        FileInputStream fis = new FileInputStream(sourcePath);
        FileOutputStream fos = new FileOutputStream(destinationPath);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        byte[] buffer = new byte[1024];
        int len = 0;

        //Extract compressed content.
        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        //Release resources.
        fos.close();
        fis.close();
        gzis.close();
        buffer = null;
    }

    public static String getPublicFilePathDowload() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public static File getPublicFilePathDowloadFile(String filename) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + filename);
    }

    public static ArrayList<String> getFolderFiles(final File folder) {
        ArrayList<String> files = new ArrayList<>();
        if (folder.listFiles() != null && folder.listFiles().length > 0) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    getFolderFiles(fileEntry);
                } else {
                    files.add(fileEntry.getName());
                }
            }
        }

        return files;
    }

    public static String getSDFilePath(Context context, Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(DOCUMENT_SEPARATOR);
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + FOLDER_SEPARATOR + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(DOCUMENT_SEPARATOR);
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !DocumentsContract.isDocumentUri(context, uri)) {
            boolean document = false;
            String folders = "", type = "", external;
            for (String segm : uri.getPathSegments()) {
                if (segm.contains(DOCUMENT_SEPARATOR)) {
                    type = segm.split(DOCUMENT_SEPARATOR)[0];
                    folders = segm.split(DOCUMENT_SEPARATOR)[1];
                    document = true;
                    break;
                }
            }
            if (type.equals("primary")) {
                external = Environment.getExternalStorageDirectory().getPath();
            } else {
                external = "/storage/" + type;
            }
            if (document) {
                return external + FOLDER_SEPARATOR + folders + FOLDER_SEPARATOR + uri.getLastPathSegment();
            } else {
                return uri.getPath();
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        Log.d(FileUtils.class.getSimpleName(), "isExternalStorageReadOnly: extStorageState = " + extStorageState);
        Log.d(FileUtils.class.getSimpleName(), "isExternalStorageReadOnly: Environment.MEDIA_MOUNTED_READ_ONLY = " + Environment.MEDIA_MOUNTED_READ_ONLY);
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        Log.d(FileUtils.class.getSimpleName(), "isExternalStorageAvailable: extStorageState = " + extStorageState);
        Log.d(FileUtils.class.getSimpleName(), "isExternalStorageAvailable: Environment.MEDIA_MOUNTED = " + Environment.MEDIA_MOUNTED);
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists() && file.isFile();
    }

    public static boolean isPathExist(String path) {
        File folder = new File(path);
        return folder.exists() || folder.mkdirs();
    }

    public static String getRealPathFromURI(String contentURI, Context context) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("_data"));
    }

    public static String getFilenameWithExtention(String path) {
        File file = new File(path);
        return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator) + 1);
    }

    public static long getFolderSize(File folder) {
        long length = 0;
        folder.mkdirs();
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += getFolderSize(file);
            }
        }
        return length;
    }

    public static String getFilePath(String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;
    }

    public static String getFileFolder(String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator) - 1) + File.separator;
    }

    public static String getFilePath(File file) {
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;
    }

    public static String getFileExtension(String path) {
        File file = new File(path);
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static void removeFileBytes(String pathname) {
        try {
            File path = new File(pathname);
            byte[] bytes = readFile(path);
            byte[] filteredByteArray = Arrays.copyOfRange(bytes, 100, bytes.length);
            FileOutputStream out = new FileOutputStream(pathname, false);
            out.write(filteredByteArray);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFileLines(String pathname, int cnt) {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            FileInputStream fstream = new FileInputStream(pathname);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                System.out.println(strLine);
                lines.add(strLine);
            }
            //Close the input stream
            in.close();
            ArrayList<String> result = new ArrayList<String>();
            result.addAll(lines);
            lines.clear();
            if (cnt < ((result.size() - 1) / 2)) {
                for (int i = cnt; i < result.size() - 1; i++) {
                    lines.add(result.get(i));
                }
                FileUtils.deleteFile(pathname);
                for (String line : lines) {
                    FileUtils.writeToFile(pathname, line, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String fileName, String content, boolean append) {
        try {
            PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(fileName), append)));
            p.println(content);
            p.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean writeToFile(Context context, String pathWithName, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(pathWithName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File saveResponseBodyToDisk(ResponseBody body, File file) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean writeStream = writeStream(inputStream, outputStream);
        Log.i(FileUtils.class.getSimpleName(), "saveResponseBodyToDisk: " + writeStream);
        return file;
    }

    private static boolean writeStream(InputStream inputStream, OutputStream outputStream) {
        boolean res = false;
        try {
            byte[] bytes = new byte[4096];
            while (true) {
                int read = inputStream.read(bytes);
                if (read == -1) {
                    break;
                }
                outputStream.write(bytes, 0, read);
            }
            outputStream.flush();
            System.out.println("Done!");
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    public static String readFromFile(Context context, String pathWithName) {
        String ret = "";
        try {
            InputStream inputStream = new FileInputStream(new File(pathWithName));
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }
            inputStream.close();
            ret = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void createDirectories(String path) {
        File SDCardRoot;
        if (path.contains(Environment.getExternalStorageDirectory().toString())) {
            SDCardRoot = new File(path);
        } else {
            SDCardRoot = new File(SDCARD_PATH + path);
        }
        Log.d(FileUtils.class.getSimpleName(), "createDirectories: " + SDCardRoot.getAbsolutePath());

        if (SDCardRoot.exists()) {
            Log.d(FileUtils.class.getSimpleName(), "createDirectories: direcory exists aready");
        } else {
            Log.e(FileUtils.class.getSimpleName(), "outcome for " + SDCardRoot.getAbsolutePath() + "     " + SDCardRoot.mkdirs());
            Log.d(FileUtils.class.getSimpleName(), "createDirectories: Directory created successfully!");
        }
        File NoMedia = new File(SDCARD_PATH + path + "/.nomedia");
        if (!NoMedia.exists()) {
            try {
                NoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean renameFile(String SavePath, String filePath) {
        return new File(filePath).renameTo(new File(SavePath));
    }

    public static void moveFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public static boolean deleteFile(String path) {
        try {
            File file = new File(path);
            boolean exists = file.exists();
            if (exists) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean cleanDirectoryRecursive(File dir) throws Exception {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                cleanDirectoryRecursive(file);
            }
            file.delete();
        }
        return true;
    }

    public static boolean cleanDirectory(String path) {
        try {
            File dir = new File(path);
            return cleanDirectoryRecursive(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getMediaType(String file) {
        Log.d("getMediaType", "file: " + file);
        Log.d("getMediaType", "file.substring: " + file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/")));
        try {
            if (file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/")).equals(".Image")) {
                return 0;
            }
            if (file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/")).equals(".Video")) {
                return 1;
            }
            if (file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/")).equals(".Audio")) {
                return 2;
            }
            if (file.substring(file.lastIndexOf("/.k/") + 4, file.lastIndexOf("/")).equals(".Documents")) {
                return 3;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String formatFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static String formatFileSize(long size, int digits) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        StringBuilder digs = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            digs.append("#");
        }
        return new DecimalFormat("#,##0." + digs.toString()).format(size
                / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    public static byte[] fileToBytes(File file) throws Exception {
        byte[] bytes = new byte[((int) file.length())];
        InputStream input = new FileInputStream(file);
        int iter = 0;
        while (iter < bytes.length) {
            int block = input.read(bytes, iter, bytes.length - iter);
            if (block < 0) {
                break;
            }
            iter += block;
        }
        input.close();
        return bytes;
    }

    public static Bitmap getBitmap(Uri mediaURI, Context context) throws IOException {
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), mediaURI);
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5File(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        StringBuilder result = new StringBuilder();
        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(String path) throws IOException {
        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        return BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
    }

    public static Bitmap getBitmap(Context context, int idRes) {
        return BitmapFactory.decodeResource(context.getResources(), idRes);
    }

    /**
     * Меняем размер bitmap и поворачиваем
     *
     * @param image   bitmap
     * @param maxWith начальная максимальная ширина
     * @return bitmap
     * @throws IOException
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxWith) throws IOException {
        int w = image.getWidth();//get width
        int h = image.getHeight();//get height
        Log.d(FileUtils.class.getSimpleName(), "getResizedBitmap:w = " + w + " h = " + h);
        double aspRat = (double) w / (double) h;//get aspect ratio
        Log.d(FileUtils.class.getSimpleName(), "getResizedBitmap: aspRat = " + aspRat);
        int H = (int) (w * aspRat);//set the height based on width and aspect ratio
        Log.d(FileUtils.class.getSimpleName(), "getResizedBitmap: H = " + H + " maxWith = " + maxWith);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, maxWith, H, true);
        Log.d(FileUtils.class.getSimpleName(), "getResizedBitmap : H = " + scaledBitmap.getHeight() + " W = " + scaledBitmap.getWidth());
        return scaledBitmap;
    }

    public static File writeBitmap(@NonNull Bitmap bitmap, File file) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bytes);
        try {
            boolean exists = file.exists();
            boolean newFile = file.createNewFile();
            if (!exists && !newFile) {
                return null;
            }
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap resizeBitmap(byte[] imageBytes, int width, int height) throws IOException {
        Bitmap bm; // prepare object to return
        // clear system and runtime of rubbish
        System.gc();
        Runtime.getRuntime().gc();
        //Decode image size only
        BitmapFactory.Options oo = new BitmapFactory.Options();
        // only decodes size, not the whole image
        // See Android documentation for more info.
        oo.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, oo);
        // Important function to resize proportionally.
        //Find the correct scale value. It should be the power of 2.
        //Decode Options: byte array image with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = calculateInSampleSize(o2, width, height);
        o2.inPurgeable = true; // for effeciency
        o2.inInputShareable = true;
        // Do actual decoding, this takes up resources and could crash
        // your app if you do not do it properly
        bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, o2);
        // Just to be safe, clear system and runtime of rubbish again!
        System.gc();
        Runtime.getRuntime().gc();
        return bm; // return Bitmap to the method that called it
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        int width = source.getWidth();
        int height = source.getHeight();
        float coef = (float) width / (float) height;
        int newWidth = (int) ((float) height * coef);
        int newHeight = (int) ((float) width * coef);
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public static Bitmap rotateBitmap1(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        int height = source.getHeight();
        int width = source.getWidth();
        float sx = 1024;
        float sy = 800;
//        matrix.setScale(sx, sy);
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public static Bitmap rotateBitmapBack(Bitmap arrowBitmap, float angle) {
        Bitmap canvasBitmap = arrowBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvasBitmap.eraseColor(0xffffffff);
        Canvas canvas = new Canvas(canvasBitmap);
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.drawBitmap(arrowBitmap, rotateMatrix, new Paint(6));
        arrowBitmap.recycle();
        return canvasBitmap;
    }

    public static Bitmap cropCenter(Bitmap bitmap) {
        int minSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int diffSize = Math.abs(bitmap.getWidth() - bitmap.getHeight());
        Bitmap targetBitmap;
        targetBitmap = Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            matrix.setTranslate(diffSize, 0);
        } else {
            matrix.setTranslate(0, diffSize);
        }
        canvas.drawBitmap(targetBitmap, new Matrix(), new Paint(6));
        bitmap.recycle();
        return targetBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        float sx = (float) wantedWidth / bitmap.getWidth();
        float sy = (float) wantedHeight / bitmap.getHeight();
        m.setScale(sx, sy);
        canvas.drawBitmap(bitmap, m, new Paint(6));
        bitmap.recycle();
        return output;
    }

    public static Bitmap translateBitmap(Bitmap bitmap, int dx, int dy) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setTranslate(dx, dy);
        canvas.drawBitmap(bitmap, m, new Paint(6));
        bitmap.recycle();
        return output;
    }

    public static String readFile(String fileName) {
        StringBuilder text = new StringBuilder();
        try {
            File file = new File(fileName);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readFile(File file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int bytes = (int) file.length();
        byte[] buffer = new byte[bytes];
        int readBytes = bis.read(buffer);
        bis.close();
        return buffer;
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static boolean writeFile(byte[] data, String fileName) throws IOException {
        File file = new File(getFilePath(fileName));
        Log.d(FileUtils.class.getSimpleName(), "writeFile to " + fileName);
        boolean folderFilesExist = file.exists() || file.mkdirs();
        Log.d(FileUtils.class.getSimpleName(), "encrypt: folderFilesExist = " + folderFilesExist);
        if (!folderFilesExist) {
            return false;
        }
        createNomedia(fileName);
        FileOutputStream out = new FileOutputStream(fileName, false);
        out.write(data);
        out.close();
        return true;
    }

    private static void createNomedia(String path) {
        File NoMedia = new File(getFilePath(path) + File.separator + ".nomedia");
        if (!NoMedia.exists()) {
            try {
                NoMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeBytesToFile(byte[] bytes, String path) throws IOException {
        if (!isPathExist(path)) {
            return;
        }
        createNomedia(path);
        File file = new File(path);
        file.createNewFile();
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } finally {
            if (bos != null) {
                try {
                    //flush and close the BufferedOutputStream
                    bos.flush();
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap compressImage(Bitmap image, int percent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, percent, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap resizeBitmap(String path, int width, int height) throws IOException {
        byte[] imageBytes = readFile(new File(path));
        Bitmap bm; // prepare object to return
        // clear system and runtime of rubbish
        System.gc();
        Runtime.getRuntime().gc();

        //Decode image size only
        BitmapFactory.Options oo = new BitmapFactory.Options();
        // only decodes size, not the whole image
        // See Android documentation for more info.
        oo.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, oo);
        //Decode Options: byte array image with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = calculateInSampleSize(o2, width, height);
        o2.inPurgeable = true; // for effeciency
        o2.inInputShareable = true;
        // Do actual decoding, this takes up resources and could crash
        // your app if you do not do it properly
        bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, o2);
        // Just to be safe, clear system and runtime of rubbish again!
        System.gc();
        Runtime.getRuntime().gc();

        return bm; // return Bitmap to the method that called it
    }

    public static Bitmap resizeBitmap(String path, int required_size) throws IOException {
        byte[] imageBytes = readFile(new File(path));
        Bitmap bm; // prepare object to return
        // clear system and runtime of rubbish
        System.gc();
        Runtime.getRuntime().gc();
        //Decode image size only
        BitmapFactory.Options oo = new BitmapFactory.Options();
        // only decodes size, not the whole image
        // See Android documentation for more info.
        oo.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, oo);
        // Important function to resize proportionally.
        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (oo.outWidth / scale / 2 >= required_size
                && oo.outHeight / scale / 2 >= required_size)
            scale *= 2; // Actual scaler
        //Decode Options: byte array image with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale; // set scaler
        o2.inPurgeable = true; // for effeciency
        o2.inInputShareable = true;
        // Do actual decoding, this takes up resources and could crash
        // your app if you do not do it properly
        bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, o2);
        // Just to be safe, clear system and runtime of rubbish again!
        System.gc();
        Runtime.getRuntime().gc();
        return bm; // return Bitmap to the method that called it
    }

    public static Bitmap resizeBitmap(byte[] imageBytes, int required_size) throws IOException {
        Bitmap bm; // prepare object to return
        // clear system and runtime of rubbish
        System.gc();
        Runtime.getRuntime().gc();
        //Decode image size only
        BitmapFactory.Options oo = new BitmapFactory.Options();
        // only decodes size, not the whole image
        // See Android documentation for more info.
        oo.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, oo);
        // Important function to resize proportionally.
        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (oo.outWidth / scale / 2 >= required_size
                && oo.outHeight / scale / 2 >= required_size)
            scale *= 2; // Actual scaler
        //Decode Options: byte array image with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale; // set scaler
        o2.inPurgeable = true; // for effeciency
        o2.inInputShareable = true;
        // Do actual decoding, this takes up resources and could crash
        // your app if you do not do it properly
        bm = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, o2);
        // Just to be safe, clear system and runtime of rubbish again!
        System.gc();
        Runtime.getRuntime().gc();
        return bm; // return Bitmap to the method that called it
    }

    public static Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, 0);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), 0);
    }

    public static byte[] bitmapToBytes(Bitmap photo) {
        return bitmapToBytes(photo, Bitmap.CompressFormat.PNG);
    }

    public static byte[] bitmapToBytes(Bitmap photo, Bitmap.CompressFormat compressFormat) {
        return bitmapToBytes(photo, compressFormat, 100);
    }

    public static byte[] bitmapToBytes(Bitmap photo, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(compressFormat, quality, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] bytes) {
        try {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getThumnailStringFromFile(String path) {
        Bitmap b = null;
        try {
            Log.d(FileUtils.class.getSimpleName(), "getThumbnailStringFromFile");
            b = ThumbnailUtils.createVideoThumbnail(path, 3);
        } catch (Exception e) {
            Log.d(FileUtils.class.getSimpleName(), "getThumbnailStringFromFile Exception: " + e.toString());
        }
        return bitmapToString(b);
    }

    public static String getPublicFilePath(int mediaType, String filename) {
        String dir = FileUtils.class.getSimpleName();
        if (mediaType == 0) {
            dir = Environment.DIRECTORY_PICTURES;
        } else if (mediaType == 1) {
            dir = Environment.DIRECTORY_MOVIES;
        } else if (mediaType == 2) {
            dir = Environment.DIRECTORY_MUSIC;
        } else if (mediaType == 3) {
            dir = Environment.DIRECTORY_DOCUMENTS;
        } else {
            dir = Environment.DIRECTORY_PICTURES;
        }
        return Environment.getExternalStoragePublicDirectory(dir).toString() + "/" + filename;
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return path.delete();
    }

    public static Bitmap textOnBitmap(Bitmap bitmap, String text, int size) {
        try {
            Bitmap.Config bitmapConfig = bitmap.getConfig();
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888;
            }
            // resource bitmaps are imutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true);
            Canvas canvas = new Canvas(bitmap);
            // new antialised Paint
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(0, 0, 0));
            // text size in pixels
            paint.setTextSize(size);
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width()) / 2;
            int y = (bitmap.getHeight() + bounds.height()) / 2;
//			Log.d(FileUtils.class.getSimpleName(), "textOnBitmap: size = " +size);
//			Log.d(FileUtils.class.getSimpleName(), "textOnBitmap: bitmap.getWidth() = " +bitmap.getWidth());
//			Log.d(FileUtils.class.getSimpleName(), "textOnBitmap: canvas.getWidth() = " +canvas.getWidth());
//			Log.d(FileUtils.class.getSimpleName(), "textOnBitmap: bounds.width() = " + bounds.width());
//			Log.d(FileUtils.class.getSimpleName(), "textOnBitmap: x = " + x + " y = " + y);
            canvas.drawText(text, x, y, paint);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Copy an InputStream to a File.
    public static boolean saveFile(InputStream in, File file) {
        OutputStream out = null;
        boolean result;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out.close();
                }
                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void copyFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Bitmap getThumb(Uri uri, Context context) {
        File image = new File(getUriFilePath(uri, context));
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
        Log.d(FileUtils.class.getSimpleName(), "getPreview: originalSize = " + originalSize);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / 2;
        return BitmapFactory.decodeFile(image.getPath(), opts);
    }

    public static HashMap<String, String> getMediaData(Context context, Uri MediaUri) {
        Log.d(FileUtils.class.getSimpleName(), "getMediaData: MediaUri = " + MediaUri);
        Cursor mediaCursor = context.getContentResolver().query(MediaUri, null, null, null, null);
        Log.d(FileUtils.class.getSimpleName(), "getMediaData: mediaCursor = " + mediaCursor);
        HashMap<String, String> data = new HashMap<>();
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                for (int i = 0; i < mediaCursor.getColumnCount(); i++) {
                    data.put(mediaCursor.getColumnName(i), mediaCursor.getString(i));
                }
            } while (mediaCursor.moveToNext());
            mediaCursor.close();
        }
        return data;
    }

    private static void removeImageThumbnails(ContentResolver contentResolver, long photoId) {
        Cursor thumbnails = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[]{String.valueOf(photoId)}, null);
        for (thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()) {

            long thumbnailId = thumbnails.getLong(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails._ID));
            String path = thumbnails.getString(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            File file = new File(path);
            if (file.delete()) {

                contentResolver.delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, MediaStore.Images.Thumbnails._ID + "=?", new String[]{String.valueOf(thumbnailId)});

            }

        }
    }

    public static String getUriFilePath(Uri mediaURI, Context context) {
        String filePath;
        // filePath= getMediaData(mediaURI, context);
        //		if (filePath == null) {
        filePath = getSDFilePath(context, mediaURI);
        Log.d(FileUtils.class.getSimpleName(), "getUriFilePath: " + filePath);
        //		}
        return filePath;
    }

    public static Bitmap getVideoThumbnail(String filePath) {
        Bitmap thumb = null;
        Bitmap thumbCompressed = null;
        try {
            thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumb;
    }

    public static byte[] getInpitStreamBytes(InputStream is) {
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getMediaThumb(String filePath, int required_size) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        byte[] rawArt;
        Bitmap thumb = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        rawArt = retriever.getEmbeddedPicture();
        if (rawArt != null) {
            thumb = resizeBitmap(rawArt, required_size);
        }
        return thumb;
    }

    public static void removeImageThumbnails(Context context, long photoId) {
        Cursor thumbnails = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Thumbnails.IMAGE_ID + "=?", new String[]{String.valueOf(photoId)}, null);
        if (thumbnails != null) {
            for (thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()) {
                long thumbnailId = thumbnails.getLong(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails._ID));
                String path = thumbnails.getString(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                File file = new File(path);
                if (file.delete()) {
                    context.getContentResolver().delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, MediaStore.Images.Thumbnails._ID + "=?", new String[]{String.valueOf(thumbnailId)});
                }
            }
            thumbnails.close();
        }
    }

    public static void removeVideoThumbnails(Context context, long photoId) {
        Log.d(FileUtils.class.getSimpleName(), "removeVideoThumbnails: photoId = " + photoId);
        Cursor thumbnails = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Thumbnails.VIDEO_ID + "=?", new String[]{String.valueOf(photoId)}, null);
        Log.d(FileUtils.class.getSimpleName(), "removeVideoThumbnails: thumbnails = " + thumbnails);
        if (thumbnails != null) {
            for (thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()) {
                long thumbnailId = thumbnails.getLong(thumbnails.getColumnIndex(MediaStore.Video.Thumbnails._ID));
                Log.d(FileUtils.class.getSimpleName(), "removeVideoThumbnails: thumbnailId = " + thumbnailId);
                String path = thumbnails.getString(thumbnails.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                File file = new File(path);
                if (file.delete()) {
                    context.getContentResolver().delete(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, MediaStore.Video.Thumbnails._ID + "=?", new String[]{String.valueOf(thumbnailId)});
                }
            }
            thumbnails.close();
        }
    }

    public static String getMediaDuration(String filePath) {
        String duration = "";
        try {
            duration = getMediaMeta(filePath, MediaMetadataRetriever.METADATA_KEY_DURATION);
            ;
            if (duration.length() <= 0) {
                return "";
            }
            long millisec = Long.parseLong(duration);
            long second = (millisec / 1000) % 60;
            long minute = (millisec / 60000) % 60;
            long hour = (millisec / 3600000) % 24;
            if (hour > 0) {
                return String.format("%02d:%02d:%02d", hour, minute, second);
            } else if (minute > 0) {
                return String.format("%02d:%02d", minute, second);
            } else {
                return String.format("%02d", second);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }

    public static String getMediaMeta(String filePath, int metaType) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        return retriever.extractMetadata(metaType);
    }

    public static Uri getFileUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
