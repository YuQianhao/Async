package com.yuqianhao.async.core;

public interface IAsync {

    IAsync io();

    IAsync ui();

    IAsync push(Runnable runnable);





}
