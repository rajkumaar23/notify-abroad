package in.co.rajkumaar.notifyabroad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {

    private static String currentState = TelephonyManager.EXTRA_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = "CallReceiver";
        try {
            if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (
                        currentState.equals(TelephonyManager.EXTRA_STATE_IDLE) &&
                        state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)
                ) {
                    Log.d(TAG, "onReceive: Outgoing call");
                    return;
                }

                boolean shouldIgnoreState =
                        !state.equals(TelephonyManager.EXTRA_STATE_RINGING) &&
                        !state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK);

                if (incomingNumber == null || incomingNumber.isEmpty() || shouldIgnoreState) {
                    return;
                }

                Log.d(TAG, "onReceive: Incoming call from " + incomingNumber);
                currentState = state;
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: Exception", e);
        }
    }
}