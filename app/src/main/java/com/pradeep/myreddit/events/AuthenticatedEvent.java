package com.pradeep.myreddit.events;

import com.pradeep.myreddit.models.User;

public class AuthenticatedEvent {

    private User user;

    public AuthenticatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
