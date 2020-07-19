package com.mufu.common;

import android.os.Handler;

import java.lang.ref.WeakReference;

public class HandlerTemp<T> extends Handler {

    protected WeakReference<T> ref;

    public HandlerTemp(T cls){
        ref = new WeakReference<>(cls);
    }

    public T getRef(){
        return ref != null ? ref.get() : null;
    }
}
