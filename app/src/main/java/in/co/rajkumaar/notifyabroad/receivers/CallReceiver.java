package in.co.rajkumaar.notifyabroad.receivers;

import static android.content.Context.MODE_PRIVATE;
import static in.co.rajkumaar.notifyabroad.Constants.NOTIFY_CALLS;
import static in.co.rajkumaar.notifyabroad.Constants.SHARED_PREFERENCES_KEY;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_BOT_TOKEN;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_CHAT_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import in.co.rajkumaar.notifyabroad.api.TelegramAPI;
import in.co.rajkumaar.notifyabroad.api.TelegramAPIResponse;

public class CallReceiver extends BroadcastReceiver {

    private static String currentState = TelephonyManager.EXTRA_STATE_IDLE;
    private final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (currentState.equals(TelephonyManager.EXTRA_STATE_IDLE) && state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    return;
                }

                boolean shouldIgnoreState = (state == null)
                        || (!state.equals(TelephonyManager.EXTRA_STATE_RINGING) && !state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK));

                if (incomingNumber == null || incomingNumber.isEmpty() || shouldIgnoreState) {
                    return;
                }

                Log.d(TAG, "onReceive: Incoming call from " + incomingNumber);
                postToTelegramAPI(context, incomingNumber, state);
                currentState = state;
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: Exception", e);
        }
    }

    private void postToTelegramAPI(Context context, String number, String state) throws JSONException {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        SHARED_PREFERENCES_KEY, MODE_PRIVATE
                );
        boolean notifyCalls = sharedPreferences.getBoolean(NOTIFY_CALLS, false);
        String botToken = sharedPreferences.getString(TELEGRAM_BOT_TOKEN, null);
        String chatID = sharedPreferences.getString(TELEGRAM_CHAT_ID, null);
        if (notifyCalls && botToken != null) {
            TelegramAPI api = new TelegramAPI(context, botToken);
            JSONObject requestBody = new JSONObject();

            requestBody.put("text",
                    "<b>Call Alert</b>\n\n"
                            + number + " - " + state
                            + "\n\nWhatsApp Link : https://api.whatsapp.com/send/?phone="
                            + (number.startsWith("+") ? number.substring(1) : number)
            );
            requestBody.put("chat_id", chatID);
            requestBody.put("parse_mode", "HTML");
            requestBody.put("disable_web_page_preview", true);
            api.sendMessage(requestBody, new TelegramAPIResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i(TAG, String.format("Call log relayed successfully : %s", response.toString()));
                }

                @Override
                public void onFailure(Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
    }
}