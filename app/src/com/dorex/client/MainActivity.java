package com.dorex.client;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQ_OVER = 10;
    private static final int REQ_USAGE = 11;
    private static final String MC = "com.mojang.minecraftpe";

    private TextView status;
    private Button btnStart;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status);
        btnStart = (Button) findViewById(R.id.btn_start);

        Button btnPerm = (Button) findViewById(R.id.btn_perm);
        Button btnUsage = (Button) findViewById(R.id.btn_usage);
        Button btnLaunch = (Button) findViewById(R.id.btn_launch);
        Button btnStop = (Button) findViewById(R.id.btn_stop);

        btnPerm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { reqOverlay(); }
        });
        btnUsage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { reqUsage(); }
        });
        btnLaunch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { launchMc(); }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startAndLaunch(); }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, DorexService.class));
                Toast.makeText(MainActivity.this, "stopped", Toast.LENGTH_SHORT).show();
            }
        });

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 20);
        }

        refresh();
    }

    @Override
    protected void onResume() { super.onResume(); refresh(); }

    private void refresh() {
        boolean mc = mcInstalled();
        boolean ov = Settings.canDrawOverlays(this);
        boolean ua = hasUsage();

        status.setText("Minecraft: " + (mc ? "OK" : "missing")
                + "\nOverlay: " + (ov ? "OK" : "missing")
                + "\nUsage Access: " + (ua ? "OK" : "missing"));

        btnStart.setEnabled(mc && ov);
    }

    private boolean mcInstalled() {
        try { getPackageManager().getPackageInfo(MC, 0); return true; }
        catch (Exception e) { return false; }
    }

    private boolean hasUsage() {
        try {
            AppOpsManager o = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int m = o.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) { return false; }
    }

    private void reqOverlay() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "already granted", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivityForResult(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())), REQ_OVER);
    }

    private void reqUsage() {
        if (hasUsage()) {
            Toast.makeText(this, "already granted", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQ_USAGE);
    }

    private void startAndLaunch() {
        Intent svc = new Intent(this, DorexService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc);
        else startService(svc);
        launchMc();
    }

    private void launchMc() {
        Intent i = getPackageManager().getLaunchIntentForPackage(MC);
        if (i != null) { startActivity(i); moveTaskToBack(true); }
        else Toast.makeText(this, "MC not installed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r, c, d);
        if (r == REQ_OVER || r == REQ_USAGE) refresh();
    }
}