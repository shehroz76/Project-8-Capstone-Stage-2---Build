package com.pradeep.myreddit.events;

import com.pradeep.myreddit.models.Post;

public class ViewCommentsEvent {

    private Post selectedPost;

    public ViewCommentsEvent(Post selectedPost) {
        this.selectedPost = selectedPost;
    }

    public Post getSelectedPost() {
        return selectedPost;
    }
}
