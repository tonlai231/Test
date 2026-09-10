package com.j8.doubletapsleep;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String PREFS = "prefs";
    private static final String KEY_ENABLED = "hotspot_enabled";
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("J8 DOUBLE TAP SLEEP");
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Không cần root. Sau khi bật Trợ năng, chạm 2 lần vào vùng ‘2×’ ở mép phải màn hình để khóa/Sleep thật.");
        info.setTextSize(16);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, dp(18), 0, dp(18));
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        Button accessibility = makeButton("1. MỞ CÀI ĐẶT TRỢ NĂNG");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(accessibility, buttonParams());

        Button enable = makeButton("2. BẬT VÙNG CHẠM 2×");
        enable.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, true).apply();
            updateStatus();
        });
        root.addView(enable, buttonParams());

        Button disable = makeButton("TẮT VÙNG CHẠM 2×");
        disable.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply();
            updateStatus();
        });
        root.addView(disable, buttonParams());

        TextView note = new TextView(this);
        note.setText("Lưu ý: vùng 2× chỉ chiếm một phần nhỏ ở mép phải để không cản thao tác cảm ứng bình thường. Màn hình khóa thật bằng Accessibility.");
        note.setTextSize(14);
        note.setTextColor(Color.GRAY);
        note.setPadding(0, dp(20), 0, 0);
        root.addView(note, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setAllCaps(false);
        return b;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56));
        p.topMargin = dp(12);
        return p;
    }

    private void updateStatus() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean hotspot = sp.getBoolean(KEY_ENABLED, true);
        boolean access = isAccessibilityEnabled();
        status.setText("Trợ năng: " + (access ? "ĐÃ BẬT" : "CHƯA BẬT") + "\nVùng 2×: " + (hotspot ? "ĐANG BẬT" : "ĐANG TẮT"));
        status.setTextColor(access && hotspot ? Color.rgb(0, 130, 70) : Color.rgb(190, 80, 0));
    }

    private boolean isAccessibilityEnabled() {
        try {
            String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabled == null) return false;
            return enabled.toLowerCase().contains(getPackageName().toLowerCase() + "/" + DoubleTapAccessibilityService.class.getName().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
