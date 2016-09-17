package com.pradeep.myreddit.events;

import com.pradeep.myreddit.models.Subreddit;

import java.util.List;

public class SubredditPreferencesUpdatedEvent {

    private List<Subreddit> subreddits;

    public SubredditPreferencesUpdatedEvent(List<Subreddit> subreddits) {
        this.subreddits = subreddits;
    }

    public List<Subreddit> getSubreddits() {
        return subreddits;
    }
}
