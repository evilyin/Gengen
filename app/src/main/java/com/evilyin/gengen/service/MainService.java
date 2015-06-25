package com.evilyin.gengen.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.evilyin.gengen.AccessTokenKeeper;
import com.evilyin.gengen.AppManager;
import com.evilyin.gengen.activity.MainActivity;

import cn.byr.bbs.sdk.api.MailApi;
import cn.byr.bbs.sdk.auth.Oauth2AccessToken;
import cn.byr.bbs.sdk.exception.BBSException;
import cn.byr.bbs.sdk.net.RequestListener;

/**
 * 主服务，设定闹钟，注册停止监听，开始搜索
 *
 * @author evilyin(ChenZhixi)
 * @since 2015-6-16
 */

public class MainService extends Service {

    private ConnectivityManager mConnectivityManager;
    private Oauth2AccessToken mAccessToken;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, AppManager.scanTime * 1000);
            startService(new Intent(MainService.this, ScanService.class));
            Log.i("MainService", "发出启动搜索intent");
        }
    };

    @Override
    public void onCreate() {
        Log.i("MainService", "主服务启动");
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (AppManager.list != null) {
            AppManager.list.clear();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mConnectivityManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        //判断wifi是否可用
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            handler.post(runnable);
            //todo 鉴权，延长token期限
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //取消任务
        handler.removeCallbacks(runnable);
        handler = null;
        if (mConnectivityManager != null) {
            mConnectivityManager = null;
        }
        //存储结果
        if (AppManager.list != null) {
            SharedPreferences preferences = this.getSharedPreferences("bbs_post_result", MODE_APPEND);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet("list", AppManager.list);
            editor.apply();

            if (AppManager.list.size() != 0) {
                //通知
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentTitle("跟跟")
                        .setContentText("sb又犯贱了")
                        .setContentIntent(pendingIntent);
                Notification notification = builder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(1, notification);

                //站内信
                MailApi mailApi = new MailApi(mAccessToken);
                mailApi.send("chihiro2B", "李老湿又犯贱了", "以下版面有帖子：" + AppManager.list, 0, 0, listener);
            }
        }
        Log.i("MainService", "主服务停止");
        stopSelf();
        super.onDestroy();
    }

    RequestListener listener = new RequestListener() {
        @Override
        public void onComplete(String s) {
            Log.i("MainService", "站内信发送成功");
        }

        @Override
        public void onException(BBSException e) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
