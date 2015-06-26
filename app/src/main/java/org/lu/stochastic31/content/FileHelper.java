package org.lu.stochastic31.content;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

class FileHelper {
    static final String DIR = Environment.getExternalStorageDirectory()
            .getPath() + "/stochastic31/";
    static final String END_SUFFIX = ".random";

    public static boolean hasSD = Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED);

    public static String[] listTitles() {
        String[] filenames = listFiles();
        if (filenames == null)
            filenames = new String[0];
        String[] titles = new String[filenames.length];
        for (int i = 0; i < filenames.length; ++i)
            titles[i] = filenames[i].replace(END_SUFFIX, "");
        return titles;
    }

    public static String[] listFiles() {
        if (hasSD)
            return listFiles(DIR, END_SUFFIX);
        return new String[]{};
    }

    public static String[] listFiles(String dir, final String end_suffix) {
        File f = new File(dir);
        if (!f.exists())
            f.mkdirs();
        return f.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.indexOf(end_suffix) > 0
                        && filename.endsWith(end_suffix.substring(1));
            }
        });
    }

    /**
     * 创建文件对象
     */
    public static File getFileInstance(String filename, boolean onSD) {
        if (onSD && hasSD)
            return new File(DIR + filename);
        return new File(filename);
    }
}
