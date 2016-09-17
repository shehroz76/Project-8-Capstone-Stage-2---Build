package com.pradeep.myreddit.events;

public class ShareContentEvent {

    private String content;
    private String mimeType;

    public ShareContentEvent(String content, String mimeType) {
        this.content = content;
        this.mimeType = mimeType;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }
}
