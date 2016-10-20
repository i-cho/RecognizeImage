package jp.co.eforce.smarteyeglass.recognizeimage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cho on 2016/10/14.
 */

public final class RecognizeImageExtensionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(Constants.LOG_TAG, "onReceive: " + intent.getAction());
        intent.setClass(context, RecognizeImageExtensionService.class);
        context.startService(intent);
    }

}
