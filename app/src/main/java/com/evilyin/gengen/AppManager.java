package com.evilyin.gengen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.evilyin.gengen.receiver.StopReceiver;
import com.evilyin.gengen.service.MainService;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * @author evilyin(ChenZhixi)
 * @since 2015-6-25
 */
public class AppManager {
    public static Set<String> list = new HashSet<>();
    public static int scanTime = 600;//搜索间隔时长（秒）
    public static String content="";//发帖内容第一行
    public static boolean sendmail=true;//是否发送站内信

    public static void registerAlarm(Context context) {
        //闹钟：开始
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context, MainService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        //闹钟：停止
        AlarmManager alarmManager2 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(context, StopReceiver.class);
        intent.setAction("com.evilyin.stop");
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 8);
        calendar2.set(Calendar.MINUTE, 30);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);
        alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent2);


    }

    public static void loadSettings(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_APPEND);
        scanTime = preferences.getInt("scantime", 600);
        content = preferences.getString("content", "sb楼主又来犯贱了");
        sendmail = preferences.getBoolean("sendmail", true);
    }
}
