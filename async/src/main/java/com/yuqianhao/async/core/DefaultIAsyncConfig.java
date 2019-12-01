package com.yuqianhao.async.core;

public class DefaultIAsyncConfig implements IAsyncConfig {
    @Override
    public int coreThreadSize() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public int maxThreadSize() {
        return Runtime.getRuntime().availableProcessors()*2;
    }

    @Override
    public int waitTime() {
        return 60;
    }
}
