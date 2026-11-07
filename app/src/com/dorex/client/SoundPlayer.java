package com.dorex.client;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundPlayer {
    private final ToneGenerator tg;

    public SoundPlayer() {
        tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
    }

    public void click() {
        try { tg.startTone(ToneGenerator.TONE_PROP_BEEP, 60); } catch (Exception ignored) {}
    }

    public void toggle() {
        try { tg.startTone(ToneGenerator.TONE_PROP_ACK, 80); } catch (Exception ignored) {}
    }

    public void release() {
        try { tg.release(); } catch (Exception ignored) {}
    }
}