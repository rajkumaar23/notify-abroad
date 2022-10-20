package in.co.rajkumaar.notifyabroad.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class TelegramAPI {
    private final String botURL;
    private final RequestQueue requestQueue;

    public TelegramAPI(Context context, String token) {
        String TELEGRAM_BASE_URL = "https://api.telegram.org";
        this.botURL = TELEGRAM_BASE_URL + "/bot" + token;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void sendMessage(JSONObject requestBody, TelegramAPIResponse apiResponse) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(
                        Request.Method.POST,
                        botURL + "/sendMessage",
                        requestBody,
                        apiResponse::onSuccess,
                        apiResponse::onFailure
                );
        requestQueue.add(jsonObjectRequest);
    }
}
