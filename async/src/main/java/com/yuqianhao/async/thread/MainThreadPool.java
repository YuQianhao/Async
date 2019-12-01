package com.yuqianhao.async.thread;

import android.os.Handler;
import android.os.Looper;

public class MainThreadPool {

    private static final Handler HANDLER=new Handler(Looper.getMainLooper());

    private static final MainThreadPool MAIN_THREAD_POOL=new MainThreadPool();

    public static final MainThreadPool getInstance(){
        return MAIN_THREAD_POOL;
    }

    public void submit(Runnable runnable){
        HANDLER.post(runnable);
    }

}
