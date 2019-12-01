package com.yuqianhao.async.core;

import android.os.Looper;

import com.yuqianhao.async.R;
import com.yuqianhao.async.model.Value;
import com.yuqianhao.async.thread.MainThreadPool;
import com.yuqianhao.async.thread.ThreadPool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SignleAsync implements ISignleAsync{

    private AsyncType asyncType;

    private Queue<Node> nodeQueue;

    private Object lock;

    private ThreadPool threadPool;

    private MainThreadPool mainThreadPool;

    private Value _value;

    public SignleAsync(){
        if(Looper.myLooper()==Looper.getMainLooper()){
            asyncType=AsyncType.UI;
        }else{
            asyncType=AsyncType.IO;
        }
        nodeQueue=new ConcurrentLinkedQueue<>();
        lock=new Object();
        threadPool=ThreadPool.getInstance();
        mainThreadPool=MainThreadPool.getInstance();
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

    private void postRun(AsyncType asyncType,Runnable runnable) throws InterruptedException {
        if(asyncType==AsyncType.IO){
            threadPool.submit(runnable);
        }else{
            mainThreadPool.submit(runnable);
        }
        synchronized (lock){
            lock.wait();
        }
    }

    @Override
    public void run() {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Node node;
                    while((node=nodeQueue.remove())!=null){
                        if(node instanceof RunnableNode){
                            final RunnableNode runnableNode= (RunnableNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    runnableNode.runnable.run();
                                    synchronized (lock){
                                        lock.notifyAll();
                                    }
                                }
                            };
                            postRun(runnableNode.asyncType,_exec);
                        }else if(node instanceof ValueHandleNode){
                            final ValueHandleNode valueHandleNode= (ValueHandleNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    setValue(valueHandleNode.asyncValue.onHandle());
                                    synchronized (lock){
                                        lock.notifyAll();
                                    }
                                }
                            };
                            postRun(valueHandleNode.asyncType,_exec);
                        }else if(node instanceof ReceiveHandlerNode){
                            final ReceiveHandlerNode receiveHandlerNode= (ReceiveHandlerNode) node;
                            Runnable _exec=new Runnable() {
                                @Override
                                public void run() {
                                    receiveHandlerNode.asyncValueHandler.onReceive(_value);
                                    synchronized (lock){
                                        lock.notifyAll();
                                    }
                                }
                            };
                            postRun(receiveHandlerNode.asyncType,_exec);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
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
}
