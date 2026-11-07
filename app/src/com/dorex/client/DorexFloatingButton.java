package com.dorex.client;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

public class DorexFloatingButton extends TextView {

    private static final float SLOP_DP = 12f;

    private final WindowManager wm;
    private final WindowManager.LayoutParams lp;
    private final DorexMenu menu;
    private final SoundPlayer sound;
    private final float density;

    private float downX, downY;
    private int startX, startY;
    private boolean dragging;

    public DorexFloatingButton(Context c, WindowManager w,
                               WindowManager.LayoutParams params,
                               DorexMenu m, SoundPlayer s) {
        super(c);
        wm = w; lp = params; menu = m; sound = s;
        density = c.getResources().getDisplayMetrics().density;

        setText("D");
        setTextColor(Color.WHITE);
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26);
        setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        setGravity(Gravity.CENTER);
        setShadowLayer(4, 2, 2, Color.BLACK);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#8B5A3C"));
        bg.setStroke((int)(3 * density), Color.parseColor("#3D2513"));
        setBackground(bg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = e.getRawX(); downY = e.getRawY();
                startX = lp.x; startY = lp.y;
                dragging = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getRawX() - downX;
                float dy = e.getRawY() - downY;
                if (!dragging && Math.hypot(dx, dy) > SLOP_DP * density) dragging = true;
                if (dragging) {
                    lp.x = startX + (int) dx;
                    lp.y = startY + (int) dy;
                    try { wm.updateViewLayout(this, lp); } catch (Exception ignored) {}
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!dragging) { sound.click(); menu.toggle(); }
                return true;
        }
        return super.onTouchEvent(e);
    }
}