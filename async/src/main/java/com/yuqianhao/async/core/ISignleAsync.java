package com.yuqianhao.async.core;

public interface ISignleAsync{

    ISignleAsync push(IAsyncValue asyncValue);

    ISignleAsync onValueHandler(IAsyncValueHandler asyncValueHandler);

    ISignleAsync io();

    ISignleAsync ui();

    ISignleAsync push(Runnable runnable);

    void run();

}
