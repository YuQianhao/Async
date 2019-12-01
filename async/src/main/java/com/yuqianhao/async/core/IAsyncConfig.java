package com.yuqianhao.async.core;

public interface IAsyncConfig {

    /**
     * 核心线程数量，这个数量的线程不会被释放
     * */
    int coreThreadSize();

    /**
     * 最大线程的数量，最大线程的数量-核心线程的数量=可能会被释放线程的数量
     * */
    int maxThreadSize();

    /**
     * 除核心线程外，其他的线程在无工作状态最多等待的秒数，超过这个时间将会被释放
     * */
    int waitTime();

}
