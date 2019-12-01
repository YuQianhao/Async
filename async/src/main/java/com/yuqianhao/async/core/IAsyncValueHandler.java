package com.yuqianhao.async.core;

import com.yuqianhao.async.model.Value;

public interface IAsyncValueHandler<_Tx> {

    void onReceive(Value<_Tx> object);

}
