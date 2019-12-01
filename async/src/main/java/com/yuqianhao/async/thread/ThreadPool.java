package com.yuqianhao.async.thread;

import android.app.Application;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private static ThreadPool threadPool=null;

    private int coreSize;

    private int maxSize;

    private int waitTime;

    private ThreadPoolExecutor threadPoolExecutorl;

    private ThreadPool(int coreSize, int maxSize, int waitTime) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.waitTime = waitTime;
        this.threadPoolExecutorl=new ThreadPoolExecutor(
                this.coreSize,
                this.maxSize,
                this.waitTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private ThreadPool(int coreSize, int maxSize) {
        this(coreSize,maxSize,60);
    }

    private ThreadPool(int coreSize) {
        this(coreSize,coreSize*2,60);
    }

    public ThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public void submit(Runnable runnable){
        threadPoolExecutorl.submit(runnable);
    }

    public static ThreadPool getInstance(){
        if(threadPool==null){
            threadPool=new ThreadPool();
        }
        return threadPool;
    }

    public static void newThreadPool(int coreSize, int maxSize, int waitTime){
        threadPool=new ThreadPool(coreSize, maxSize, waitTime);
    }



}
