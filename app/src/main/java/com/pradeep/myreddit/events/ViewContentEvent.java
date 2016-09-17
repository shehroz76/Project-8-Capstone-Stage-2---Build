package com.pradeep.myreddit.events;

public class ViewContentEvent {

    private String contentTitle;
    private String url;

    public ViewContentEvent(String contentTitle, String url) {
        this.contentTitle = contentTitle;
        this.url = url;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getUrl() {
        return url;
    }
}
