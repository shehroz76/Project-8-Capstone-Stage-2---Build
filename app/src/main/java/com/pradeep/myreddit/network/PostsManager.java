package com.pradeep.myreddit.network;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pradeep.myreddit.App;
import com.pradeep.myreddit.R;
import com.pradeep.myreddit.models.Post;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostsManager extends RedditManager {

    private static final String TAG = PostsManager.class.getSimpleName();

    public static void getPosts(String subreddit, String sort, String after, final Callback<List<Post>> callback) {
        sort = sort.toLowerCase();

        // build posts url
        String postsUrl = AuthManager.isUserAuthenticated() ? AUTH_API_BASE : UNAUTH_API_BASE;
        if (subreddit.equals(App.getAppContext().getResources().getString(R.string.front_page))) {
            postsUrl += "/" + sort + ".json";
        } else {
            postsUrl += "/r/" + subreddit + "/" + sort + ".json";
        }
        if (after != null) {
            postsUrl += "?after=" + after;
        }

        AsyncHttpClient client = getAsyncHttpClient(true);
        if (client == null) {
            if (callback != null) callback.onFailure(null);
            return;
        }

        client.get(postsUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");

                    String after = data.getString("after");
                    Log.i(TAG, after);
                    JSONArray children = data.getJSONArray("children");

                    List<Post> posts = new ArrayList<Post>();

                    for (int i=0; i<children.length(); i++) {;
                        JSONObject postData = children.getJSONObject(i).getJSONObject("data");

                        Post post = new Post();
                        post.setId(postData.getString("id"));
                        post.setFullName(postData.getString("name"));
                        post.setLikes(!postData.isNull("likes") ? postData.getBoolean("likes") : null);
                        post.setDomain(postData.getString("domain"));
                        post.setSubreddit(postData.getString("subreddit"));
                        post.setAuthor(postData.getString("author"));
                        post.setScore(postData.getInt("score"));
                        post.setCreated(postData.getString("created_utc"));
                        post.setNsfw(postData.getBoolean("over_18"));
                        post.setThumbnail(postData.getString("thumbnail"));
                        post.setUrl(postData.getString("url"));
                        post.setTitle(postData.getString("title"));
                        post.setNumComments(postData.getInt("num_comments"));
                        post.setPermalink(postData.getString("permalink"));
                        post.setIsSelf(postData.getBoolean("is_self"));
                        post.setSelftext(postData.getString("selftext_html"));

                        if (i == children.length()-1) {
                            post.setAfter(after.equals("null") ? null : after);
                        } else {
                            post.setAfter(null);
                        }

                        posts.add(post);
                    }

                    if (callback != null) callback.onSuccess(posts);
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
