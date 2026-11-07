package com.dorex.client;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AddonManager {

    private final Context ctx;

    public AddonManager(Context c) { this.ctx = c; }

    public void installAll() {
        install("entity_culling.mcpack");
        install("fps_boost.mcpack");
        install("durability.mcpack");
    }

    private void install(String name) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory(),
                    "Android/data/com.mojang.minecraftpe/files/games/com.mojang/resource_packs");
            if (!dir.exists()) dir.mkdirs();

            File out = new File(dir, name);
            InputStream in = ctx.getAssets().open("addons/" + name);
            OutputStream os = new FileOutputStream(out);
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            os.close(); in.close();

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(out), "application/octet-stream");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "addon " + name + " missing",
                    Toast.LENGTH_LONG).show();
        }
    }
}