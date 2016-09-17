package com.pradeep.myreddit.network;

import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pradeep.myreddit.App;
import com.pradeep.myreddit.R;
import com.pradeep.myreddit.models.Subreddit;
import com.pradeep.myreddit.utils.Helpers;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubredditsManager extends RedditManager {

    private static final String TAG = PostsManager.class.getSimpleName();
    private static final String SUBREDDITS_PREFS_KEY = "subreddits_prefs_key";

    public static void getSubreddits(final Callback<List<Subreddit>> callback) {
        Map<String, String> urls = new HashMap<String, String>();
        urls.put("defaults", "/subreddits/default.json?limit=100");
        urls.put("user", "/reddits/mine.json?limit=100");

        // check if requested subreddits are already cached or not
        // if yes, simply return them, else send request to server and then cache them
        Gson gson = new Gson();
        String subredditsString = Helpers.readFromPrefs(App.getAppContext(), SUBREDDITS_PREFS_KEY);
        if (subredditsString != null) {
            List<Subreddit> subreddits = Arrays.asList(gson.fromJson(subredditsString, Subreddit[].class));
            if (callback != null) callback.onSuccess(subreddits);
        } else {
            String subredditsUrl;
            if (AuthManager.isUserAuthenticated()) {
                subredditsUrl = AUTH_API_BASE + urls.get("user");
            } else {
                subredditsUrl = UNAUTH_API_BASE + urls.get("defaults");
            }

            AsyncHttpClient client = getAsyncHttpClient(true);
            if (client == null) {
                if (callback != null) callback.onFailure(null);
                return;
            }

            client.get(subredditsUrl, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONArray children = response.getJSONObject("data").getJSONArray("children");

                        List<Subreddit> subreddits = new ArrayList<Subreddit>();

                        // add Front Page as first tab
                        subreddits.add(new Subreddit(null,
                                App.getAppContext().getResources().getString(R.string.front_page), true));

                        for (int i = 0; i < children.length(); i++) {
                            JSONObject subredditData = children.getJSONObject(i).getJSONObject("data");

                            Subreddit subreddit = new Subreddit(subredditData.getString("id"),
                                    subredditData.getString("display_name"), true);

                            subreddits.add(subreddit);

                            saveSubreddits(subreddits);
                        }

                        if (callback != null) callback.onSuccess(subreddits);
                    } catch (JSONException e) {
                        if (callback != null)
                            callback.onFailure(App.getAppContext().getResources().getString(R.string.error_parsing));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (callback != null) callback.onFailure(String.valueOf(statusCode));
                }
            });
        }
    }

    public static void saveSubreddits(List<Subreddit> subreddits) {
        Gson gson = new Gson();
        Helpers.writeToPrefs(App.getAppContext(), SUBREDDITS_PREFS_KEY, gson.toJson(subreddits));
    }
}
