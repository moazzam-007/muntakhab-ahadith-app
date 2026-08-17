package com.moazzam.muntakhabahadith.utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetCopier {

    /**
     * Copies a file from assets to the app's cache directory and returns the File.
     */
    public static File copyAssetToCache(Context context, String assetFileName) throws Exception {
        File cachedFile = new File(context.getCacheDir(), assetFileName);
        
        // Return existing if already copied
        if (cachedFile.exists() && cachedFile.length() > 0) {
            return cachedFile;
        }

        File tempFile = new File(context.getCacheDir(), assetFileName + ".tmp");
        try (InputStream in = context.getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(tempFile)) {
             
            byte[] buffer = new byte[1024 * 4];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        
        tempFile.renameTo(cachedFile);
        return cachedFile;
    }
}
