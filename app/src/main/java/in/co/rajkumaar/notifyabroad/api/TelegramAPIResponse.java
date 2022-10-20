package in.co.rajkumaar.notifyabroad.api;

import org.json.JSONObject;

public abstract class TelegramAPIResponse {
    public abstract void onSuccess(JSONObject response);
    public abstract void onFailure(Exception exception);
}
