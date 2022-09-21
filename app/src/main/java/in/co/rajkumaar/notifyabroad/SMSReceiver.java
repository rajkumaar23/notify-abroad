package in.co.rajkumaar.notifyabroad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = "SMSReceiver";
        try {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pDus = (Object[]) bundle.get("pdus");
                    if (pDus.length == 0) {
                        return;
                    }
                    // Large message might be broken into many
                    SmsMessage[] messages = new SmsMessage[pDus.length];
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pDus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pDus[i]);
                        sb.append(messages[i].getMessageBody());
                    }
                    String sender = messages[0].getOriginatingAddress();
                    String message = sb.toString();
                    Log.d(TAG, "onReceive: Incoming SMS from " + sender);
                    Log.d(TAG, "onReceive: Incoming SMS body : " + message);
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "onReceive: Exception ", exception);
        }
    }
}