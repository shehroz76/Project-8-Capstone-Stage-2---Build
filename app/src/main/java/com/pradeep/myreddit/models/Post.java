package com.pradeep.myreddit.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.pradeep.myreddit.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

public class Post implements Parcelable {

    private String id;
    private String fullName;
    private String domain;
    private String subreddit;
    private String author;
    private int score;
    private boolean isNsfw;
    private String thumbnail;
    private String created;
    private String title;
    private String url;
    private int numComments;
    private String permalink;
    private boolean isSelf;
    private String selftext;
    private int likes;
    private String after;

    public Post() {
        // empty constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isNsfw() {
        return isNsfw;
    }

    public void setNsfw(boolean isNsfw) {
        this.isNsfw = isNsfw;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        List<String> disallowedTuhmbnails = new ArrayList<String>();
        disallowedTuhmbnails.add("");
        disallowedTuhmbnails.add("default");
        disallowedTuhmbnails.add("self");
        disallowedTuhmbnails.add("nsfw");

        if (disallowedTuhmbnails.contains(thumbnail)) {
            thumbnail = null;
        } else {
            this.thumbnail = thumbnail;
        }
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = Helpers.humanizeTimestamp(created);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getSelftext() {
        return selftext;
    }

    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(Boolean likes) {
        if (likes == null) {
            this.likes = 0;
        } else if (likes) {
            this.likes = 1;
        } else {
            this.likes = -1;
        }
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    /**
     * Parcelable stuff
     */

    public static final Creator<Post> CREATOR = new Creator<Post>() {

        @Override
        public Post createFromParcel(Parcel parcel) {
            return new Post(parcel);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(fullName);
        parcel.writeString(domain);
        parcel.writeString(subreddit);
        parcel.writeString(author);
        parcel.writeInt(score);
        parcel.writeInt(isNsfw ? 1 : 0);
        parcel.writeString(thumbnail);
        parcel.writeString(created);
        parcel.writeString(title);
        parcel.writeString(url);
        parcel.writeInt(numComments);
        parcel.writeString(permalink);
        parcel.writeString(selftext);
        parcel.writeInt(likes);
        parcel.writeString(after);
        parcel.writeInt(isSelf ? 1 : 0);
    }

    public Post(Parcel source) {
        id = source.readString();
        fullName = source.readString();
        domain = source.readString();
        subreddit = source.readString();
        author = source.readString();
        score = source.readInt();
        isNsfw = source.readInt() == 1;
        thumbnail = source.readString();
        created = source.readString();
        title = source.readString();
        url = source.readString();
        numComments = source.readInt();
        permalink = source.readString();
        selftext = source.readString();
        likes = source.readInt();
        after = source.readString();
        isSelf = source.readInt() == 1;
    }
}
