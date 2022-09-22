package in.co.rajkumaar.notifyabroad;

import static android.content.Context.MODE_PRIVATE;
import static in.co.rajkumaar.notifyabroad.Constants.NOTIFY_SMS;
import static in.co.rajkumaar.notifyabroad.Constants.SHARED_PREFERENCES_KEY;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_BOT_TOKEN;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_CHAT_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SMSReceiver extends BroadcastReceiver {
    private final String TAG = "SMSReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
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
                    postToTelegramAPI(context, sender, message);
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "onReceive: Exception ", exception);
        }
    }

    private void postToTelegramAPI(Context context, String sender, String message) throws JSONException {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        SHARED_PREFERENCES_KEY, MODE_PRIVATE
                );
        boolean notifySMS = sharedPreferences.getBoolean(NOTIFY_SMS, false);
        String botToken = sharedPreferences.getString(TELEGRAM_BOT_TOKEN, null);
        String chatID = sharedPreferences.getString(TELEGRAM_CHAT_ID, null);
        if (notifySMS && botToken != null) {
            TelegramAPI api = new TelegramAPI(context, botToken);
            JSONObject requestBody = new JSONObject();
            requestBody.put(
                    "text",
                    "<b>SMS Alert</b> \n\n" + sender + " sent : \n\n" + message
            );
            requestBody.put("chat_id", chatID);
            requestBody.put("parse_mode", "HTML");
            requestBody.put("disable_web_page_preview", true);
            api.sendMessage(requestBody, new TelegramAPIResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.v(TAG, response.toString());
                }

                @Override
                public void onFailure(Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
    }
}