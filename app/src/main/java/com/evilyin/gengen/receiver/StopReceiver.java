package com.evilyin.gengen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evilyin.gengen.service.ScanService;

/**
 * @author evilyin(ChenZhixi)
 * @since 2015-6-16
 */

public class StopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, ScanService.class));
    }
}
