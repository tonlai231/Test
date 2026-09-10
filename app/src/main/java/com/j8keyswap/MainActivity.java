package com.j8keyswap;

import android.app.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  TextView status, log;
  Button on, off, reboot;

  @Override public void onCreate(Bundle b){ super.onCreate(b); buildUi(); checkRoot(); }

  void buildUi(){
    LinearLayout v=new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); v.setPadding(36,36,36,36);
    TextView t=new TextView(this); t.setText("J8 KEY SWAP"); t.setTextSize(28); t.setGravity(Gravity.CENTER); v.addView(t);
    TextView d=new TextView(this); d.setText("\nVolume +  →  POWER\nPOWER  →  Tăng âm lượng\nVolume -  →  Giữ nguyên\n"); d.setTextSize(18); d.setGravity(Gravity.CENTER); v.addView(d);
    status=new TextView(this); status.setText("Đang kiểm tra root..."); status.setTextSize(18); status.setGravity(Gravity.CENTER); v.addView(status);
    on=new Button(this); on.setText("BẬT HOÁN ĐỔI"); on.setOnClickListener(x->swap(true)); v.addView(on);
    off=new Button(this); off.setText("TẮT / KHÔI PHỤC"); off.setOnClickListener(x->swap(false)); v.addView(off);
    reboot=new Button(this); reboot.setText("KHỞI ĐỘNG LẠI"); reboot.setOnClickListener(x->new AlertDialog.Builder(this).setTitle("Khởi động lại?").setMessage("Máy sẽ reboot ngay để áp dụng thay đổi.").setNegativeButton("Hủy",null).setPositiveButton("Reboot",(a,z)->new Thread(()->su("reboot")).start()).show()); v.addView(reboot);
    log=new TextView(this); log.setTextSize(13); log.setTextIsSelectable(true); v.addView(log);
    ScrollView s=new ScrollView(this); s.addView(v); setContentView(s);
  }

  void checkRoot(){
    new Thread(()->{
      R r=su("id");
      runOnUiThread(()->{ if(r.code==0 && r.out.contains("uid=0")){ status.setText("ROOT: OK"); status.setTextColor(Color.rgb(0,130,70)); } else { status.setText("CHƯA CÓ QUYỀN ROOT"); status.setTextColor(Color.RED); } log.setText(r.out); });
    }).start();
  }

  void swap(boolean enable){
    on.setEnabled(false); off.setEnabled(false); status.setText("Đang xử lý...");
    new Thread(()->{
      String cmd=enable? enableCommand() : "rm -rf /data/adb/modules/j8_keyswap_app; echo OK";
      R r=su(cmd);
      runOnUiThread(()->{
        log.setText(r.out);
        if(r.code==0 && r.out.contains("OK")){ status.setText(enable?"ĐÃ BẬT - HÃY REBOOT":"ĐÃ TẮT - HÃY REBOOT"); status.setTextColor(enable?Color.rgb(0,130,70):Color.DKGRAY); Toast.makeText(this,"Thành công. Hãy khởi động lại máy.",Toast.LENGTH_LONG).show(); }
        else { status.setText("CÓ LỖI - XEM BÊN DƯỚI"); status.setTextColor(Color.RED); }
        on.setEnabled(true); off.setEnabled(true);
      });
    }).start();
  }

  String enableCommand(){
    return "SRC=''; " +
      "for f in /vendor/usr/keylayout/gpio-keys.kl /vendor/usr/keylayout/gpio_keys.kl /system/vendor/usr/keylayout/gpio-keys.kl /system/vendor/usr/keylayout/gpio_keys.kl /system/usr/keylayout/gpio-keys.kl /system/usr/keylayout/gpio_keys.kl /odm/usr/keylayout/gpio-keys.kl /odm/usr/keylayout/gpio_keys.kl; do " +
      "[ -f \"$f\" ] || continue; grep -qE '^[[:space:]]*key[[:space:]]+115[[:space:]]+VOLUME_UP([[:space:]]|$)' \"$f\" && grep -qE '^[[:space:]]*key[[:space:]]+116[[:space:]]+POWER([[:space:]]|$)' \"$f\" && { SRC=\"$f\"; break; }; done; " +
      "[ -n \"$SRC\" ] || { echo ERROR:no_keylayout; exit 11; }; " +
      "M=/data/adb/modules/j8_keyswap_app; rm -rf \"$M\"; " +
      "case \"$SRC\" in /vendor/*) D=\"$M/system/vendor/${SRC#/vendor/}\";; /system/vendor/*) D=\"$M/system/vendor/${SRC#/system/vendor/}\";; /system/*) D=\"$M/system/${SRC#/system/}\";; /odm/*) D=\"$M/system/odm/${SRC#/odm/}\";; *) echo ERROR:path; exit 12;; esac; " +
      "mkdir -p \"$(dirname \"$D\")\" || exit 13; cp -af \"$SRC\" \"$D\" || exit 14; " +
      "sed -e 's/^[[:space:]]*key[[:space:]][[:space:]]*115[[:space:]][[:space:]]*VOLUME_UP.*$/key 115 POWER WAKE/' -e 's/^[[:space:]]*key[[:space:]][[:space:]]*116[[:space:]][[:space:]]*POWER.*$/key 116 VOLUME_UP/' \"$D\" > \"$D.tmp\" || exit 15; mv \"$D.tmp\" \"$D\"; chmod 0644 \"$D\"; " +
      "printf 'id=j8_keyswap_app\\nname=J8 Key Swap\\nversion=1.0\\nversionCode=1\\nauthor=J8 Key Swap App\\ndescription=Volume Up becomes Power; Power becomes Volume Up.\\n' > \"$M/module.prop\"; chmod 0644 \"$M/module.prop\"; " +
      "grep -qE '^[[:space:]]*key[[:space:]]+115[[:space:]]+POWER([[:space:]]|$)' \"$D\" && grep -qE '^[[:space:]]*key[[:space:]]+116[[:space:]]+VOLUME_UP([[:space:]]|$)' \"$D\" || { rm -rf \"$M\"; echo ERROR:verify; exit 16; }; echo OK";
  }

  R su(String cmd){ StringBuilder o=new StringBuilder(); int c=-1; try{ Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start(); BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)); String l; while((l=br.readLine())!=null)o.append(l).append('\n'); c=p.waitFor(); }catch(Exception e){o.append("ERROR: ").append(e); } return new R(c,o.toString()); }
  static class R{ int code; String out; R(int c,String o){code=c;out=o;} }
}
