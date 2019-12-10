package com.yuqianhao.async.core;

public class AsyncType {

    public static final AsyncType IO=new AsyncType(0);

    public static final AsyncType UI=new AsyncType(1);

    private int type;

    public AsyncType(int type) {
        this.type = type;
    }

    public void setType(int type){
        this.type=type;
    }

    public int getType(){
        return this.type;
    }

    @Override
    public boolean equals(Object obj) {
        return obj!=null && ((AsyncType)obj).type==type;
    }
}
