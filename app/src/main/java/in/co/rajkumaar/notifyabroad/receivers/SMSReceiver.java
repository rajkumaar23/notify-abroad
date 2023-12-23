package in.co.rajkumaar.notifyabroad.receivers;

import static android.content.Context.MODE_PRIVATE;
import static in.co.rajkumaar.notifyabroad.Constants.ARE_FILTERS_ENABLED;
import static in.co.rajkumaar.notifyabroad.Constants.FILTERS_LIST;
import static in.co.rajkumaar.notifyabroad.Constants.NOTIFY_SMS;
import static in.co.rajkumaar.notifyabroad.Constants.SHARED_PREFERENCES_KEY;
import static in.co.rajkumaar.notifyabroad.Constants.SMS_FILTERS;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_BOT_TOKEN;
import static in.co.rajkumaar.notifyabroad.Constants.TELEGRAM_CHAT_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.co.rajkumaar.notifyabroad.api.TelegramAPI;
import in.co.rajkumaar.notifyabroad.api.TelegramAPIResponse;

public class SMSReceiver extends BroadcastReceiver {
    private final String TAG = "SMSReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (action != null && action.equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pDus = (Object[]) bundle.get("pdus");
                    if (pDus == null || pDus.length == 0) {
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

                    boolean skipMessage = false;
                    SharedPreferences prefs = context.getSharedPreferences(SMS_FILTERS, MODE_PRIVATE);
                    if (prefs.getBoolean(ARE_FILTERS_ENABLED, false)) {
                        String filterListString = prefs.getString(FILTERS_LIST, "");
                        if (!filterListString.isEmpty()) {
                            String messageLowerCase = message.toLowerCase();
                            try {
                                skipMessage = true;
                                JSONArray filtersList = new JSONArray(filterListString);
                                for (int i = 0; i < filtersList.length(); i++) {
                                    String filter = filtersList.getString(i);
                                    if (messageLowerCase.contains(filter)) {
                                        skipMessage = false;
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, String.format("filtersList parsing failed %s", e));
                                e.printStackTrace();
                            }
                        }
                    }

                    if (!skipMessage) {
                        postToTelegramAPI(context, sender, message);
                    } else {
                        Log.i(TAG, String.format("SMS relaying skipped because no filter is present : '%s'", message));
                    }
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "onReceive: Exception ", exception);
        }
    }

    private void postToTelegramAPI(Context context, String sender, String message) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE);
        boolean notifySMS = sharedPreferences.getBoolean(NOTIFY_SMS, false);
        String botToken = sharedPreferences.getString(TELEGRAM_BOT_TOKEN, null);
        String chatID = sharedPreferences.getString(TELEGRAM_CHAT_ID, null);
        if (notifySMS && botToken != null) {
            TelegramAPI api = new TelegramAPI(context, botToken);
            JSONObject requestBody = new JSONObject();
            requestBody.put("text", "<b>SMS Alert</b> \n\n" + sender + " sent : \n\n" + message);
            requestBody.put("chat_id", chatID);
            requestBody.put("parse_mode", "HTML");
            requestBody.put("disable_web_page_preview", true);
            api.sendMessage(requestBody, new TelegramAPIResponse() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i(TAG, String.format("SMS relayed successfully : %s", response.toString()));
                }

                @Override
                public void onFailure(Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
    }
}