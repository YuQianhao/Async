package com.yuqianhao.async.core;

import com.yuqianhao.async.model.Value;

public interface IAsyncValue<_Tx> {

    Value<_Tx> onHandle();

}
