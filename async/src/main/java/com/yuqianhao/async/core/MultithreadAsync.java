package com.yuqianhao.async.core;

import android.os.Looper;

import com.yuqianhao.async.thread.MainThreadPool;
import com.yuqianhao.async.thread.ThreadPool;

public class MultithreadAsync implements IAsync{

    public static MultithreadAsync getInstance(){
        return new MultithreadAsync();
    }

    private AsyncType multithreadType;

    public MultithreadAsync(){
        if(Looper.myLooper()==Looper.getMainLooper()){
            multithreadType=AsyncType.UI;
        }else{
            multithreadType=AsyncType.IO;
        }
    }

    @Override
    public IAsync io() {
        multithreadType=AsyncType.IO;
        return this;
    }

    @Override
    public IAsync ui() {
        multithreadType=AsyncType.UI;
        return this;
    }

    @Override
    public IAsync push(Runnable runnable) {
        if(multithreadType==AsyncType.UI){
            MainThreadPool.getInstance().submit(runnable);
        }else{
            ThreadPool.getInstance().submit(runnable);
        }
        return this;
    }

}
