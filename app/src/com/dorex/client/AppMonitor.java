package com.dorex.client;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

public class AppMonitor {

    public interface Listener {
        void onForegroundChanged(boolean mcForeground);
    }

    private static final String MC = "com.mojang.minecraftpe";
    private static final long POLL_MS = 1000;

    private final UsageStatsManager usm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private boolean running = false;
    private boolean lastState = false;

    public AppMonitor(Context c, Listener l) {
        usm = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        listener = l;
    }

    public void start() {
        if (running) return;
        running = true;
        handler.post(poll);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(poll);
    }

    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!running) return;
            boolean fg = isMcFg();
            if (fg != lastState) {
                lastState = fg;
                listener.onForegroundChanged(fg);
            }
            handler.postDelayed(this, POLL_MS);
        }
    };

    private boolean isMcFg() {
        long now = System.currentTimeMillis();
        List<UsageStats> s = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - 60000, now);
        if (s == null) return false;
        String top = null;
        long last = 0;
        for (UsageStats u : s) {
            if (u.getLastTimeUsed() > last) {
                last = u.getLastTimeUsed();
                top = u.getPackageName();
            }
        }
        return MC.equals(top);
    }
}