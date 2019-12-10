package com.yuqianhao.async.core;

import com.yuqianhao.async.thread.ThreadPool;

public class Async {

    public static final void initConfig(IAsyncConfig asyncConfig){
        ThreadPool.newThreadPool(asyncConfig.coreThreadSize(),asyncConfig.maxThreadSize(),asyncConfig.waitTime());
    }

    public static final IAsync createMultithreadAsync(){
        return MultithreadAsync.getInstance();
    }

    public static final ISignleAsync createSignleAsync(){
        return new SignleAsync();
    }

}
