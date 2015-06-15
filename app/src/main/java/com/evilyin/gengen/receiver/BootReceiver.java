package com.evilyin.gengen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evilyin.gengen.service.ScanService;

/**
 * @author evilyin(ChenZhixi)
 * @since 2015-6-3
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ScanService.class));
    }
}
