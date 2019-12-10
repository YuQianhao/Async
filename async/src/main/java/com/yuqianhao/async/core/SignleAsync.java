package com.yuqianhao.async.core;

import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;

import com.yuqianhao.async.R;
import com.yuqianhao.async.model.Value;
import com.yuqianhao.async.thread.MainThreadPool;
import com.yuqianhao.async.thread.ThreadPool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SignleAsync implements ISignleAsync{

    private AsyncType asyncType;

    private Queue<Node> nodeQueue;

    private Lock lock;

    private ConditionVariable condition;

    private ThreadPool threadPool;

    private MainThreadPool mainThreadPool;

    private Value _value;

    private boolean isDoneExec;

    public SignleAsync(){
        if(Looper.myLooper()==Looper.getMainLooper()){
            asyncType=AsyncType.UI;
        }else{
            asyncType=AsyncType.IO;
        }
        nodeQueue=new ConcurrentLinkedQueue<>();
        lock=new ReentrantLock();
        condition=new ConditionVariable(true);
        threadPool=ThreadPool.getInstance();
        mainThreadPool=MainThreadPool.getInstance();
        isDoneExec=false;
    }


    @Override
    public ISignleAsync io() {
        asyncType=AsyncType.IO;
        return this;
    }

    @Override
    public ISignleAsync ui() {
        asyncType=AsyncType.UI;
        return this;
    }

    @Override
    public ISignleAsync push(IAsyncValue asyncValue) {
        ValueHandleNode valueHandleNode=new ValueHandleNode();
        valueHandleNode.asyncType=this.asyncType;
        valueHandleNode.asyncValue=asyncValue;
        nodeQueue.add(valueHandleNode);
        return this;
    }

    @Override
    public ISignleAsync onValueHandler(IAsyncValueHandler asyncValueHandler) {
        ReceiveHandlerNode receiveHandlerNode=new ReceiveHandlerNode();
        receiveHandlerNode.asyncType=this.asyncType;
        receiveHandlerNode.asyncValueHandler=asyncValueHandler;
        nodeQueue.add(receiveHandlerNode);
        return this;
    }

    @Override
    public ISignleAsync push(Runnable runnable) {
        RunnableNode runnableNode=new RunnableNode();
        runnableNode.asyncType=this.asyncType;
        runnableNode.runnable=runnable;
        nodeQueue.add(runnableNode);
        return this;
    }

    private synchronized <_Tx> void setValue(Value<_Tx> value){
        this._value=value;
    }

    private void runExector(final Exector exector){
        lock.lock();
        if(exector.asyncType.getType()==AsyncType.IO.getType()){
            try{
                exector.runnable.run();
            }catch (Exception e){e.printStackTrace();}
        }else{
            mainThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        exector.runnable.run();
                    }catch (Exception e){e.printStackTrace();}
                    finally {
                        condition.open();
                    }
                }
            });
            condition.close();
            condition.block();
        }
        lock.unlock();
    }

    @Override
    public void run() {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Node node;
                    Exector exector=null;
                    while((node=nodeQueue.poll())!=null){
                        if(node instanceof RunnableNode){
                            final RunnableNode runnableNode= (RunnableNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    runnableNode.runnable.run();
                                }
                            };
                            exector=new Exector();
                            exector.asyncType=runnableNode.asyncType;
                            exector.runnable=_exec;
                        }else if(node instanceof ValueHandleNode){
                            final ValueHandleNode valueHandleNode= (ValueHandleNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    setValue(valueHandleNode.asyncValue.onHandle());
                                }
                            };
                            exector=new Exector();
                            exector.asyncType=valueHandleNode.asyncType;
                            exector.runnable=_exec;
                        }else if(node instanceof ReceiveHandlerNode){
                            final ReceiveHandlerNode receiveHandlerNode= (ReceiveHandlerNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    receiveHandlerNode.asyncValueHandler.onReceive(_value);
                                }
                            };
                            exector=new Exector();
                            exector.asyncType=receiveHandlerNode.asyncType;
                            exector.runnable=_exec;
                        }
                        if(exector==null){continue;}
                        runExector(exector);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized boolean isDoneExec() {
        return isDoneExec;
    }

    public synchronized void setDoneExec(boolean doneExec) {
        if(doneExec){Log.e("YThread","signal");}
        isDoneExec = doneExec;
    }


    private static class Node{
        public AsyncType asyncType;
    }

    private static class RunnableNode extends Node{
        public Runnable runnable;
    }

    private static class ValueHandleNode extends Node{
        public IAsyncValue asyncValue;
    }

    private static class ReceiveHandlerNode extends Node{
        public IAsyncValueHandler asyncValueHandler;
    }

    private static class Exector{
        public AsyncType asyncType;
        public Runnable runnable;
    }
}
