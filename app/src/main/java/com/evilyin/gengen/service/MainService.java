package com.evilyin.gengen.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.evilyin.gengen.receiver.StopReceiver;

import java.util.Calendar;

/**
 * 主服务，设定闹钟，注册停止监听，开始搜索
 *
 * @author evilyin(ChenZhixi)
 * @since 2015-6-16
 */

public class MainService extends Service {

    @Override
    public void onCreate() {
        //注册停止监听
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.evilyin.stop");
        this.registerReceiver(new StopReceiver(), filter);

        //闹钟：开始
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, ScanService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 50);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        //闹钟：停止
        AlarmManager alarmManager2 = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setClass(this, StopReceiver.class);
        intent.setAction("com.evilyin.stop");
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 8);
        calendar2.set(Calendar.MINUTE, 25);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);
        alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent2);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
