package com.yuqianhao.async.model;

import androidx.annotation.NonNull;

public class Value<_Tx> {

    _Tx value;

    public Value(_Tx object){
        this.value=object;
    }

    _Tx get(){
        return value;
    }

    void set(_Tx object){
        this.value=object;
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }
}
