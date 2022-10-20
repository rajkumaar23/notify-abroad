package in.co.rajkumaar.notifyabroad.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class GitHubAPI {
    private final String repoURL;
    private final RequestQueue requestQueue;

    public GitHubAPI(Context context, String repoOwnerUsername, String repoName) {
        String GITHUB_BASE_URL = "https://api.github.com/repos";
        this.repoURL = String.format("%s/%s/%s", GITHUB_BASE_URL, repoOwnerUsername, repoName);
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getLatestReleaseVersion(GitHubAPIResponse apiResponse) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(
                        Request.Method.GET,
                        repoURL + "/releases/latest",
                        null,
                        apiResponse::onSuccess,
                        apiResponse::onFailure
                );
        requestQueue.add(jsonObjectRequest);
    }
}
