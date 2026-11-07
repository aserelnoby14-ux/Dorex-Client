package com.dorex.client;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DorexMenu {

    private final Context ctx;
    private final WindowManager wm;
    private final DorexConfig cfg;
    private final SoundPlayer sound;
    private final WindowManager.LayoutParams lp;
    private final float d;
    private LinearLayout root;
    private boolean visible = false;

    public DorexMenu(Context c, WindowManager w, DorexConfig cf, SoundPlayer s) {
        ctx = c; wm = w; cfg = cf; sound = s;
        d = c.getResources().getDisplayMetrics().density;
        lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.CENTER;
    }

    private void build() {
        root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = px(18);
        root.setPadding(pad, pad, pad, pad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#B38B5A3C"));
        bg.setStroke(px(4), Color.parseColor("#3D2513"));
        root.setBackground(bg);

        TextView title = new TextView(ctx);
        title.setText("DOREX");
        title.setTextColor(Color.parseColor("#FFAA00"));
        title.setTextSize(24);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setShadowLayer(3, 2, 2, Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, px(16));
        root.addView(title);

        root.addView(toggle("Entity Culling", cfg.isEntityCulling(), new Runnable() {
            public void run() {
                cfg.setEntityCulling(!cfg.isEntityCulling());
                sound.toggle();
                refresh();
            }
        }));

        root.addView(toggle("FPS Optimization", cfg.isFpsOpt(), new Runnable() {
            public void run() {
                cfg.setFpsOpt(!cfg.isFpsOpt());
                sound.toggle();
                refresh();
            }
        }));

        root.addView(toggle("Item & Armor Durability", cfg.isDurability(), new Runnable() {
            public void run() {
                cfg.setDurability(!cfg.isDurability());
                sound.toggle();
                refresh();
            }
        }));

        TextView close = new TextView(ctx);
        close.setText("X");
        close.setTextColor(Color.WHITE);
        close.setTextSize(18);
        close.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        close.setGravity(Gravity.CENTER);
        close.setShadowLayer(2, 2, 2, Color.BLACK);
        GradientDrawable cb = new GradientDrawable();
        cb.setColor(Color.parseColor("#AA2222"));
        cb.setStroke(px(2), Color.BLACK);
        close.setBackground(cb);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(px(44), px(44));
        clp.gravity = Gravity.CENTER_HORIZONTAL;
        clp.topMargin = px(16);
        close.setLayoutParams(clp);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { sound.click(); hide(); }
        });
        root.addView(close);
    }

    private View toggle(String label, boolean on, final Runnable action) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = px(12);
        row.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                px(280), LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = px(8);
        row.setLayoutParams(rlp);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(on ? Color.parseColor("#3E5C29") : Color.parseColor("#4A4A4A"));
        bg.setStroke(px(2), Color.BLACK);
        row.setBackground(bg);
        row.setClickable(true);

        TextView t = new TextView(ctx);
        t.setText(label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        t.setShadowLayer(1, 1, 1, Color.BLACK);
        t.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);

        TextView state = new TextView(ctx);
        state.setText(on ? "ON" : "OFF");
        state.setTextColor(on ? Color.parseColor("#55FF55") : Color.parseColor("#FF5555"));
        state.setTextSize(13);
        state.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        state.setShadowLayer(1, 1, 1, Color.BLACK);
        state.setPadding(px(8), 0, 0, 0);
        row.addView(state);

        row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { action.run(); }
        });

        return row;
    }

    private void refresh() {
        boolean was = visible;
        if (was) hide();
        build();
        if (was) show();
    }

    public void show() {
        if (visible) return;
        build();
        try { wm.addView(root, lp); visible = true; } catch (Exception ignored) {}
    }

    public void hide() {
        if (!visible || root == null) return;
        try { wm.removeView(root); } catch (Exception ignored) {}
        visible = false;
    }

    public void toggle() { if (visible) hide(); else show(); }
    public boolean isVisible() { return visible; }

    private int px(float dp) { return (int)(dp * d); }
}