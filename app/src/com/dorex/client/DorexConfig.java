package com.dorex.client;

import android.content.Context;
import android.content.SharedPreferences;

public class DorexConfig {
    private static final String PREF = "dorex_cfg";
    private final SharedPreferences sp;

    public DorexConfig(Context c) {
        sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isEntityCulling() { return sp.getBoolean("ec", false); }
    public boolean isFpsOpt()        { return sp.getBoolean("fps", false); }
    public boolean isDurability()    { return sp.getBoolean("dur", false); }

    public void setEntityCulling(boolean v) { sp.edit().putBoolean("ec", v).apply(); }
    public void setFpsOpt(boolean v)        { sp.edit().putBoolean("fps", v).apply(); }
    public void setDurability(boolean v)    { sp.edit().putBoolean("dur", v).apply(); }
}