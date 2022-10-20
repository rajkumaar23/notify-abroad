package in.co.rajkumaar.notifyabroad.api;

import org.json.JSONObject;

public abstract class GitHubAPIResponse {
    public abstract void onSuccess(JSONObject response);
    public abstract void onFailure(Exception exception);
}
