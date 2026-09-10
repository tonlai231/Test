package com.j8.doubletapsleep;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

public class DoubleTapAccessibilityService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREFS = "prefs";
    private static final String KEY_ENABLED = "hotspot_enabled";
    private WindowManager windowManager;
    private View hotspot;
    private long lastTap = 0L;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        getSharedPreferences(PREFS, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
        if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ENABLED, true)) {
            showHotspot();
        }
    }

    private void showHotspot() {
        if (hotspot != null) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        TextView v = new TextView(this);
        v.setText("2×");
        v.setTextSize(18);
        v.setTextColor(Color.WHITE);
        v.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(150, 0, 0, 0));
        bg.setCornerRadius(dp(18));
        v.setBackground(bg);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                dp(52),
                dp(84),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.x = dp(2);

        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                long now = System.currentTimeMillis();
                if (now - lastTap <= 450) {
                    lastTap = 0L;
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                } else {
                    lastTap = now;
                }
                return true;
            }
            return true;
        });

        hotspot = v;
        windowManager.addView(hotspot, lp);
    }

    private void hideHotspot() {
        if (hotspot != null && windowManager != null) {
            try {
                windowManager.removeView(hotspot);
            } catch (Exception ignored) {
            }
            hotspot = null;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!KEY_ENABLED.equals(key)) return;
        if (sharedPreferences.getBoolean(KEY_ENABLED, true)) showHotspot();
        else hideHotspot();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        hideHotspot();
        getSharedPreferences(PREFS, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        hideHotspot();
        getSharedPreferences(PREFS, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
