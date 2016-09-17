package com.pradeep.myreddit.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pradeep.myreddit.App;
import com.pradeep.myreddit.R;
import com.pradeep.myreddit.events.AccessTokenExpiredEvent;
import com.pradeep.myreddit.models.User;

import org.apache.http.*;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public class RedditManager {

    protected static final String UNAUTH_API_BASE = "https://www.reddit.com";
    protected static final String AUTH_API_BASE = "https://oauth.reddit.com";
    protected static final String USER_AGENT = "android:com.pradeep.myreddit:v1.0.0 (by /u/krnprdp)";

    protected static AsyncHttpClient getAsyncHttpClient(boolean authenticationRequired) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setUserAgent(USER_AGENT);

        if (authenticationRequired) {
            // add authorization header if user is authenticated and their access token hasn't expired yet
            User user = AuthManager.getUser();
            if (user != null) {
                if (!user.hasAccessTokenExpired()) {
                    client.addHeader("Authorization", "bearer " + user.getAccessToken());
                } else {
                    EventBus.getDefault().post(new AccessTokenExpiredEvent());
                    return null;
                }
            }
        }

        return client;
    }

    protected static void checkAccessToken(User user, Callback callback) {
        if (user.hasAccessTokenExpired()) {
            EventBus.getDefault().post(new AccessTokenExpiredEvent());
        }
    }

    public static void vote(String itemFullName, int voteDir, final Callback<Void> callback) {
        if (itemFullName == null) throw new IllegalArgumentException("'itemId' cannot be null");

        RequestParams params = new RequestParams();
        params.put("id", itemFullName);
        params.put("dir", voteDir);

        if (AuthManager.isUserAuthenticated()) {
            AsyncHttpClient client = getAsyncHttpClient(true);
            if (client == null) {
                if (callback != null) callback.onFailure(null);
                return;
            }

            client.post(AUTH_API_BASE + "/api/vote", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (callback != null) callback.onSuccess(null);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (callback != null) callback.onFailure(errorResponse.toString());
                }
            });
        } else {
            if (callback != null)
                callback.onFailure(App.getAppContext().getResources().getString(R.string.error_not_authorized));
        }
    }
}
