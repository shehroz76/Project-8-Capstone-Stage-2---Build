package com.pradeep.myreddit.network;

public interface Callback<T> {
    void onSuccess(T data);

    void onFailure(String message);
}
