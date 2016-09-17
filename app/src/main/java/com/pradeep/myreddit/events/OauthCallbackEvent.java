package com.pradeep.myreddit.events;

public class OauthCallbackEvent {

    private String code;
    private String state;
    private String error;

    public OauthCallbackEvent(String code, String state, String error) {
        this.code = code;
        this.state = state;
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public String getState() {
        return state;
    }

    public String getError() {
        return error;
    }
}

