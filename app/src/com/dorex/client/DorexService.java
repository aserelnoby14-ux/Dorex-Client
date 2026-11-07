package com.dorex.client;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class DorexService extends Service implements AppMonitor.Listener {

    private static final int NOTIF_ID = 42;
    private static final String CH = "dorex";

    private WindowManager wm;
    private DorexFloatingButton btn;
    private DorexMenu menu;
    private WindowManager.LayoutParams lp;
    private AppMonitor monitor;
    private SoundPlayer sound;
    private DorexConfig cfg;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        startForeground(NOTIF_ID, buildNotif());
        if (btn == null) attach();
        return START_STICKY;
    }

    private void attach() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        float density = getResources().getDisplayMetrics().density;
        int size = (int)(56 * density);

        cfg = new DorexConfig(this);
        sound = new SoundPlayer();
        menu = new DorexMenu(this, wm, cfg, sound);

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        lp = new WindowManager.LayoutParams(
                size, size, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 40;
        lp.y = 200;

        btn = new DorexFloatingButton(this, wm, lp, menu, sound);
        try { wm.addView(btn, lp); } catch (Exception e) { stopSelf(); }

        monitor = new AppMonitor(this, this);
        monitor.start();
    }

    @Override
    public void onForegroundChanged(boolean fg) {
        if (btn == null) return;
        btn.setVisibility(fg ? View.VISIBLE : View.GONE);
        if (!fg) menu.hide();
    }

    private Notification buildNotif() {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(new NotificationChannel(
                        CH, "Dorex", NotificationManager.IMPORTANCE_MIN));
            }
            b = new Notification.Builder(this, CH);
        } else {
            b = new Notification.Builder(this);
        }
        return b.setContentTitle("Dorex Client")
                .setContentText("active in Minecraft")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true).build();
    }

    @Override
    public void onDestroy() {
        if (monitor != null) monitor.stop();
        if (menu != null) menu.hide();
        if (btn != null && wm != null) {
            try { wm.removeView(btn); } catch (Exception ignored) {}
        }
        if (sound != null) sound.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}