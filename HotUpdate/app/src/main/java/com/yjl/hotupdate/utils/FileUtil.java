package com.yjl.hotupdate.utils;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 文件操作类.
 *
 * @author
 */
public class FileUtil {
    private static final String TAG = "FileDao";
    private static final int MAX_RESURSION_DEEP = 30; // 最大递归深度
    private static final String NOMEDIA = ".nomedia"; // .nomedia文件，屏蔽多媒体文件显示在图库、播放列表等
    private static final String APP_WORK_PATH = "BieGame";

    private FileUtil() {

    }

    /**
     * SD卡是否可用。
     *
     * @return boolean
     */
    public static boolean isSDCardAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            return true;
        }

        return false;
    }

    /**
     * 第二块SD卡是否可用。
     *
     * @return boolean
     */
    public static boolean isSecondSDCardAvailable() {
        return !TextUtils.isEmpty(getSecondSDCardPath());
    }

    /**
     * 获取SD卡可用的根目录.
     *
     * @return SD卡根目录
     */
    public static String getSDRootPath() {
        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        if (isSecondSDCardAvailable()) {
            rootPath = Environment.getExternalStorageDirectory().getParent();
            if (Environment.isExternalStorageEmulated()) {
                rootPath = new File(rootPath).getParent();
            }
        }
        return rootPath;
    }

    /**
     * 获取第一块SD卡路径。
     *
     * @return string 第一块SD卡的绝对路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取第二块SD卡路径,目前只考虑最多有两个SD卡的情况。
     *
     * @return string 第二块SD卡的绝对路径
     */
    public static String getSecondSDCardPath() {
        if (isSDCardAvailable()) {
            String parentPath = Environment.getExternalStorageDirectory()
                    .getParent();

            if (!TextUtils.isEmpty(parentPath)) {
                File externalRoot = new File(parentPath);
                // 列出外部SD卡的根目录文件，一般为/storage
                File[] files = externalRoot.listFiles();

                for (int i = 0, length = files.length; i < length; i++) {
                    // 过滤不可读取的USB设备，过滤第一块SD卡的路径
                    if (files[i].isDirectory()
                            && files[i].canRead()
                            && files[i].length() > 0
                            && !files[i].getAbsolutePath().equals(
                            getSDCardPath())) {
                        Log.v(TAG,
                                "getSecondSDCardPath, file:"
                                        + files[i].getAbsolutePath());
                        return files[i].getAbsolutePath();
                    }
                }
            }
        }

        return null;
    }

    public static String getLargerSDCard() {
        String sp = FileUtil.getSecondSDCardPath();
        String fp = FileUtil.getSDCardPath();
        if (fp == null) {
            return null;
        } else if (sp == null) {
            return fp;
        } else {
            return new File(fp).getUsableSpace() > new File(sp)
                    .getUsableSpace() ? fp : sp;
        }
    }

    /**
     * 获取SD卡上建立的工作目录，一般为/sdcard/BaiduYuedu/。
     *
     * @return string
     */
    public static String getAppWorkPath() {
        // TODO 兼容用户将工作路径设在第二SD卡上的情况
        return getLargerSDCard() + File.separator + APP_WORK_PATH;
    }

    /**
     * 递归建立目录。
     *
     * @param dirPath 要建立的目录路径
     * @return boolean
     */
    public static boolean makeDir(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            Log.w(TAG, "makeDir, dirPath is empty, return false");
            return false;
        }

        File dir = new File(dirPath);

        if (isDirExist(dir)) {
            return true;
        }

        return dir.mkdirs();
    }

    public static boolean renameAndDel(File file) {
        if (null == file) {
            return false;
        }

        File tmpFile = new File(file.getParent() + File.separator
                + System.currentTimeMillis());

        file.renameTo(tmpFile);
        return delFile(tmpFile);
    }

    /**
     * 删除文件或目录。
     *
     * @param filePath 文件绝对路径
     * @return boolean
     */
    public static boolean delFile(String filePath) {
        if (StringUtil.isStringParamEmpty(filePath)) {
            Log.w(TAG, "delFile, filePath is empty, return false");
            return false;
        }

        return delFile(new File(filePath));
    }

    /**
     * 删除文件或目录。
     *
     * @param file 要删除的文件
     * @return boolean
     */
    public static boolean delFile(File file) {
        if (!isFileExist(file)) {
            return true;
        }

        if (file.isDirectory()) {
            return removeDirRecurion(file.getAbsolutePath(), 0);
        }

        return file.delete();
    }

    /**
     * 拷贝文件。
     *
     * @param srcFile    源文件
     * @param targetFile 目标文件
     * @return boolean
     */
    public static boolean copyFile(File srcFile, File targetFile) {
        if (!isFileExist(srcFile)) {
            Log.w(TAG, "copyFile, srcFile not exist, return false.");
            return false;
        }

        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {
            if (!isFileExist(targetFile)) {
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
            }
            fcIn = new FileInputStream(srcFile).getChannel();
            fcOut = new FileOutputStream(targetFile).getChannel();
            fcIn.transferTo(0, fcIn.size(), fcOut);
            return true;
        } catch (FileNotFoundException e) {
            Log.w(TAG,
                    "copyFile, fileNotFound, targetFile:"
                            + targetFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (null != fcIn) {
                    fcIn.close();
                }
                if (null != fcOut) {
                    fcOut.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, e2.getMessage(), e2);
            }
        }

        return false;
    }

    /**
     * 递归删除指定目录的文件。
     *
     * @param dirPath     目录绝对路径
     * @param currentDeep 当前递归深度
     * @return boolean
     */
    private static boolean removeDirRecurion(String dirPath, int currentDeep) {
        if (StringUtil.isStringParamEmpty(dirPath)) {
            Log.w(TAG, "removeDir, dirPath is empty, return false");
            return false;
        }

        File dir = new File(dirPath);

        // 目录不存在，直接返回true
        if (!dir.exists()) {
            return true;
        }

        boolean result = false;
        File[] files = dir.listFiles();

        // 空目录或文件，直接删除自己后返回
        if (null == files) {
            return dir.delete();
        }

        // 删除子目录及文件
        for (int i = 0, length = files.length; i < length; i++) {
            if (files[i].isDirectory() && currentDeep < MAX_RESURSION_DEEP) {
                result = removeDirRecurion(files[i].getAbsolutePath(),
                        currentDeep + 1);
            } else {
                result = delFile(files[i]);
            }
        }

        // 删除当前目录
        result = dir.delete();

        dir = null; // 递归优化，防止OOM
        files = null;

        return result;
    }

    /**
     * 重命名文件.
     *
     * @param file        要被重命名的文件
     * @param newFileName 新的文件名，无需完整路径
     * @return boolean
     * @throws FileNotFoundException 文件未找到
     */
    public static boolean rename(File file, String newFileName)
            throws FileNotFoundException {
        if (null == file || TextUtils.isEmpty(newFileName)) {
            Log.w(TAG, "rename, file or newFileName is null, return false");
            return false;
        }

        if (!isFileExist(file)) {
            Log.w(TAG, "rename, file not exist, return false");
            return false;
        }

        String newFilePath = file.getParent() + File.separator + newFileName;
        return file.renameTo(new File(newFilePath));
    }

    /**
     * 获取工作路径的可用空间。
     *
     * @return long 单位byte
     */
    public static long getAvailableSize() {
        return getAvailableSize(getAppWorkPath());
    }

    /**
     * 获取指定目录的可用空间大小，如path为sd卡，通常为整个sd卡的可用空间，如path为内部存储空间，则返回指定目录受系统配额限制的可用空间。
     *
     * @param path 目录绝对路径
     * @return long 单位byte
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableSize(String path) {
        if (StringUtil.isStringParamEmpty(path)) {
            Log.w(TAG, "getAvailableSize, path is empty, return 0");
            return 0;
        }

        try {
            StatFs statFs = new StatFs(path);

            return (long) statFs.getAvailableBlocks() * statFs.getBlockSize();
            // new statFs(path)时，如果指定的目录path不存在，会报IllegalArgumentException
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getAvailableSize path:" + path
                    + " is not exist, return 0");
            return 0;
        }
    }

    /**
     * 检查指定目录是否有足够空间。
     *
     * @param path 目录绝对路径
     * @param size 空间大小单位byte
     * @return boolean
     */
    public static boolean hasEnoughSpace(String path, long size) {
        return getAvailableSize(path) > size;
    }

    /**
     * 获取指定目录占用空间大小。
     *
     * @param dirPath 目录绝对路径
     * @return long 单位byte
     */
    public static long getDirSize(String dirPath) {
        return getDirSizeRecurion(dirPath, 0);
    }

    /**
     * 递归计算指定目录占用空间大小。
     *
     * @param dirPath     目录绝对路径
     * @param currentDeep 本次递归的深度
     * @return long 单位byte
     */
    private static long getDirSizeRecurion(String dirPath, int currentDeep) {
        if (StringUtil.isStringParamEmpty(dirPath)) {
            Log.w(TAG, "getDirSize, path is empty, return 0");
            return 0L;
        }

        File dir = new File(dirPath);

        if (!isDirExist(dir)) {
            Log.w(TAG, "getDirSize, dirPath not exist or not a dir, return 0");
            return 0L;
        }

        File[] files = dir.listFiles();

        if (null == files) {
            return 0L;
        }

        long totalSize = 0L;

        for (int i = 0, length = files.length; i < length; i++) {
            if (files[i].isDirectory() && currentDeep < MAX_RESURSION_DEEP) {
                totalSize += getDirSizeRecurion(files[i].getAbsolutePath(),
                        currentDeep + 1);
            } else {
                totalSize += getFileSize(files[i]);
            }
        }

        dir = null; // 递归优化，防止OOM
        files = null;

        return totalSize;
    }

    /**
     * 获取文件大小。
     *
     * @param filePath 文件绝对路径
     * @return long 单位byte
     */
    public static long getFileSize(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            Log.w(TAG, "getFileSize, filePath is empty, return 0");
            return 0;
        }

        return getFileSize(new File(filePath));
    }

    /**
     * 获取文件大小，只能获取文件的大小，要获取目录的大小 如果为目录，直接返回0。
     *
     * @param file 文件
     * @return long 单位byte
     */
    public static long getFileSize(File file) {
        if (!isFileExist(file)) {
            Log.w(TAG,
                    "getFileSize, file is not exist, file:"
                            + file.getAbsolutePath() + ", return 0");
            return 0;
        }

        if (file.isDirectory()) {
            Log.w(TAG, "getFileSize, file is dir, return 0");
            return 0;
        }

        return file.length();
    }

    /**
     * 使指定目录中的media文件不显示在图库、视频库中。
     *
     * @param dirPath 目录绝对路径
     * @return boolean
     */
    public static boolean makeDirNoMedia(String dirPath) {
        if (StringUtil.isStringParamEmpty(dirPath)) {
            Log.w(TAG, "makeDirNoMedia, dirPath is empty, return false");
            return false;
        }

        File dir = new File(dirPath);

        if (!dir.exists()) {
            makeDir(dirPath);
        }

        File file = new File(dir, NOMEDIA);
        if (isFileExist(file)) {
            return true;
        }

        boolean result = false;

        try {
            result = file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return result;
    }

    /**
     * 获取指定目录的文件列表。
     *
     * @param dirPath 目录绝对路径
     * @return LinkedList
     */
    public static LinkedList<File> getFileList(String dirPath) {
        return findFileByFilterNonRecursion(dirPath, null);
    }

    /**
     * 根据keyword查找文件，不包含目录。
     *
     * @param dirPath   目录绝对路径
     * @param keyword   要查找的关键字（不支持空格，星号）
     * @param recursion 是否递归查找
     * @return LinkedList
     */
    public static LinkedList<File> findFileByKeyword(String dirPath,
                                                     final String keyword, final boolean recursion) {
        return findFileByFilter(dirPath, new FileFilter() {
            // private Pattern regexp = Pattern.compile(keyword);

            @Override
            public boolean accept(File pathname) {
                // return regexp.matcher(pathname.getName()).matches();
                return !pathname.isDirectory()
                        && pathname.getName().indexOf(keyword) >= 0;
            }
        }, recursion);
    }

    /**
     * 根据文件过滤器查找文件，如果文件不能为目录，客户端应在filter中定义。
     *
     * @param dirPath   目录绝对路径
     * @param filter    过滤器，若为null，表示不做任何过滤
     * @param recursion 是否递归查找
     * @return LinkedList
     */
    public static LinkedList<File> findFileByFilter(String dirPath,
                                                    FileFilter filter, boolean recursion) {
        if (recursion) {
            return findFileByFilterRecursion(dirPath, filter, 0);
        } else {
            return findFileByFilterNonRecursion(dirPath, filter);
        }
    }

    /**
     * 根据文件过滤器查找文件，非递归查找。
     *
     * @param dirPath 目录绝对路径
     * @param filter  过滤器，若为null，表示不做任何过滤
     * @return LinkedList
     */
    private static LinkedList<File> findFileByFilterNonRecursion(
            String dirPath, FileFilter filter) {
        if (StringUtil.isStringParamEmpty(dirPath)) {
            Log.w(TAG, "findFileByFilter, dirPath is empty, return null");
            return null;
        }

        File dir = new File(dirPath);

        if (!isDirExist(dir)) {
            Log.w(TAG,
                    "findFileByFilter, dirPath is not exist or not a dir, return null");
            return null;
        }

        LinkedList<File> fileList = new LinkedList<File>();

        File[] files = dir.listFiles(filter);
        Collections.addAll(fileList, files);

        return fileList;
    }

    /**
     * 根据文件过滤器查找文件，递归查找。
     *
     * @param dirPath     目录绝对路径
     * @param filter      过滤器，若为null，表示不做任何过滤
     * @param currentDeep 当前递归深度
     * @return LinkedList
     */
    private static LinkedList<File> findFileByFilterRecursion(String dirPath,
                                                              FileFilter filter, int currentDeep) {
        if (StringUtil.isStringParamEmpty(dirPath)) {
            Log.w(TAG, "findFileByFilter, dirPath is empty");
            return null;
        }

        File dir = new File(dirPath);

        if (!isDirExist(dir)) {
            Log.w(TAG,
                    "findFileByFilter, dirPath is not exist or not a dir, return null");
            return null;
        }

        File[] findFiles = dir.listFiles(filter);

        File[] subDir = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        LinkedList<File> fileList = new LinkedList<File>();
        if (null != findFiles && findFiles.length > 0) {
            Collections.addAll(fileList, findFiles);
        }

        if (null != subDir && subDir.length > 0) {
            for (int i = 0, length = subDir.length; i < length; i++) {
                if (currentDeep < MAX_RESURSION_DEEP) {
                    fileList.addAll(findFileByFilterRecursion(
                            subDir[i].getAbsolutePath(), filter,
                            currentDeep + 1));
                }
            }
        }

        dir = null;
        findFiles = null;
        subDir = null;

        return fileList;
    }

    /**
     * 检查目录是否存在，并且为目录文件。
     *
     * @param dir 要检查的目录文件
     * @return boolean
     */
    public static boolean isDirExist(File dir) {
        return isFileExist(dir) && dir.isDirectory();
    }

    /**
     * 检查文件是否存在，已做非空检查，file为null时认为文件不存在。
     *
     * @param file 要检查的文件
     * @return boolean
     */
    public static boolean isFileExist(File file) {
        if (null == file) {
            Log.w(TAG, "isFileExist, file is null, return false");
            return false;
        }
        return file.exists();
    }

    /**
     * 检查文件是否存在，已做非空检查，file为null时认为文件不存在。
     *
     * @param path 要检查的文件
     * @return boolean
     */
    public static boolean isFileExist(String path) {

        if (TextUtils.isEmpty(path)) {
            Log.w(TAG, "isFileExist, file is null, return false");
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    public static boolean download(String Url, String path) {
        try {
            if (Url == null || TextUtils.isEmpty(Url)) {
                Log.e(TAG, "url  is null");
                return false;
            }
            URL url = new URL(Url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream in = conn.getInputStream();
            File temp = new File(path);
            FileOutputStream fo = new FileOutputStream(temp);
            byte[] buffer = new byte[10 * 1024];
            int c = 0;
            while ((c = in.read(buffer)) != -1)
                fo.write(buffer, 0, c);
            in.close();
            fo.flush();
            fo.close();
            return true;
        } catch (MalformedURLException e) {
            LogHelper.catchExceptions(e);
        } catch (Exception e) {
            LogHelper.catchExceptions(e);
        }
        return false;
    }
}
