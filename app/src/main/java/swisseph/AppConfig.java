/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2018-01
 */

package swisseph;


import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Yura Krymlov
 * @version 1.0, 2018-01
 */
public final class AppConfig {
    public static final String EPHE_PATH = "ephe";

    final Activity activity;

    public AppConfig(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public File appEpheFolder() {
        return appExternalFilesDir(null, EPHE_PATH);
    }

    private File appHomeSubFolder(String subFolder, boolean makeIt) {
        final File homeFolder = appHomeFolder();
        if (null == homeFolder || null == subFolder) return homeFolder;
        if (homeFolder.getName().equalsIgnoreCase(subFolder)) return homeFolder;

        final File folder = new File(homeFolder, subFolder);
        if (makeIt && !folder.exists()) folder.mkdirs();
        return folder;
    }

    private File appHomeFolder() {
        File storage = Environment.getExternalStorageDirectory();

        if (null == storage || !storage.exists() || !storage.canWrite()) {
            return appExternalFilesDir(null, "home");
        }

        if (!storage.exists()) return null;
        if (!storage.canRead()) return null;
        if (!storage.canWrite()) return null;

        final File homeFolder = new File(storage, "swisseph");
        if (!homeFolder.exists()) homeFolder.mkdirs();
        return homeFolder;
    }

    private File appExternalFilesDir(String type, String folderName) {
        File filesDir = activity.getExternalFilesDir(type);
        if (null == filesDir) filesDir = appHomeSubFolder(type, false);
        if (!isBlank(folderName)) filesDir = new File(filesDir, folderName);
        if (!filesDir.exists()) filesDir.mkdirs();
        return filesDir;
    }

    public void extractAssets(String assetsDir, File assetsDest) {
        if (null == assetsDest || assetsDir == null) return;

        try {
            final AssetManager assetManager = activity.getAssets();
            for (String assetFile : assetManager.list(assetsDir)) {
                File assetFileDest = new File(assetsDest, getName(assetFile));
                String assetFilePath = concat(assetsDir, assetFile);
                if (assetFileDest.isFile()) continue;

                OutputStream out = new FileOutputStream(assetFileDest);
                InputStream in = assetManager.open(assetFilePath);
                IOUtils.copyLarge(in, out);

                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(in);
            }
        } catch (IOException ioex) {
            Log.e(assetsDir, "FAILED to extract assets", ioex);
        }
    }
}
