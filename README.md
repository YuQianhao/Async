# Async

![](https://github.com/YuQianhao/Async/blob/master/background.png?raw=true)

## Async是什么？

Async是基于Java线程池的一个应用于Android的线程调度器。

## 为什么要使用线程池？

​	多线程机制使应用能够更加充分和合理的使用CPU，内存，网络或者IO等这些系统资源，而创建一个线程需要开辟虚拟机栈，本地方法栈，程序计数器等线程私有的内存空间。在县城销毁的时候需要回收这些资源，频繁的创建和销毁线程会浪费大量的系统资源，从而最终影响应用程序的性能，增加并发编程的风险。

​	在Java中可以使用Thread类来快速创建一个线程，例如：

```java
public class Main{
    public static void main(String[] args){
        Thread thread=new Thread(()->{
    		//线程执行的内容
		});
		thread.start();
    }
}
```

​	通过Thread的构造方法，可以轻松创建一个子线程，但是这会出现一个问题，如果我们的应用程序可能需要频繁的使用到子线程，比如Android的网络请求，IO，图片处理等耗时操作，每次都去new一个Thread都会造成很大的资源开销（详见jvm源代码的Thread.cc类），而且子线程并不是创建多少个就会有多少个真实异步的子线程，例如我们拥有一颗8核心的超线程CPU，这种CPU最多允许8个逻辑线程同时存在，如果高于8个线程，线程将会采用轮训和优先级处理的方式去执行，所以，开启大量的子线程不但没意义，而且会降低线程的执行。实际上在子线程中处理的任务性能消耗还远不及创建和销毁一个子线程的，这样就会造成不必要的性能消耗，所以我们需要创建一个或者多个常驻内存的线程来不断的执行我们传递过去的任务，这样线程池就诞生了。

​	线程池通过缓存和复用已经创建的线程，从而避免开启和销毁大量线程浪费不必要的资源。

## 如何依赖？

[![](https://www.jitpack.io/v/YuQianhao/Async.svg)](https://www.jitpack.io/#YuQianhao/Async)

1、首先在项目根级目录中的build.gradle中添加：

```text
allprojects {
		repositories {
			maven { url 'https://www.jitpack.io' }
		}
	}
```

2、然后在要依赖这个框架的Module的build.gradle中添加：

```text
implementation 'com.github.YuQianhao:Async:1.0.3'
```

## 如何使用？

Async提供了两种调度器和一个属性配置方法：

* 初始化默认使用的线程池

- 线性顺序执行器
- 无序执行器

### 1、初始化默认使用的线程池

在使用Async这个执行器之前，需要了解一下如何初始化它的线程池属性，虽然这不是必须的，因为有默认的属性。

Async提供了静态方法initConfig()来初始化线程池属性，这个方法的定义如下：

```java
public static final void initConfig(IAsyncConfig asyncConfig){
	...
}
```

Async提供了一个IAsyncConfig接口类来设置线程池属性：

```java
public interface IAsyncConfig {

    
    int coreThreadSize();

   
    int maxThreadSize();

    
    int waitTime();

}
```

* coreThreadSize()

  核心处理任务的线程数，当线程池被初始化的时候就立即创建这个数量的线程，等待任务，这个数量的线程是永远不会被释放，只是在等待任务去执行。

  通常来讲应该设置为CPU核心的数量。

* maxThreadSize()

  处理任务线程数量的最大值，当核心线程都在使用的时候，线程池会在额外创建一个子线程去执行任务，额外创建的子线程的数量不会超过 maxThreadSize() - coreThreadSize() 这个数量，当额外的线程执行完成后，会在指定时间内如果没任务执行则释放线程，这个时间由方法waitTime()方法指定。

* waitTime()

  额外创建的线程在空闲时间的等待时间，如果超过这个时间，额外创建的线程将会被释放，这个时间以秒为单位。

通常来讲可以不调用这个方法，因为线程池拥有默认的属性：

coreThreadSize=CPU核心数量

maxThreadSize=CPU核心数量*2

waitTime=60秒

如果要进行自定义设定，则只需要在Application的onCreate中调用即可：

```java
public class TestApplication extends Application implements IAsyncConfig {

    @Override
    public void onCreate() {
        super.onCreate();
        Async.initConfig(this);
    }

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
```

### 2、线性顺序执行器

无论是在主线程执行还是在子线程执行，都会从第一个执行单元一直顺序执行到最后一个执行单元。

我们可以通过Async提供的工厂方法获得一个线性顺序执行器。

```java
public class Main{
    public static void main(String[] args){
        ISignleAsync signAsync=Async.createSignleAsync();
    }
}
```

createSignleAsync()函数为我们创建了一个ISignleAsync接口实例，这个接口定义了线性顺序执行器所提供的方法

```java
public interface ISignleAsync{

    ISignleAsync push(IAsyncValue asyncValue);

    ISignleAsync onValueHandler(IAsyncValueHandler asyncValueHandler);

    ISignleAsync io();

    ISignleAsync ui();

    ISignleAsync push(Runnable runnable);

    void run();

}
```

* io()

  这个方法会将接下来的执行单元切换至子线程中执行。

* ui()

  这个方法会将接下来的执行单元切换至主线程中执行。

* push(Runnable runnable)

  向执行器中添加一个执行单元，它所执行的线程环境依赖于当前执行单元的线程环境。

* push(IAsyncValue asyncValue)

  向执行器中添加一个可以向下传递值的执行单元，它所执行的线程环境依赖于当前执行单元的线程环境。

* onValueHandler(IAsyncValueHandler asyncValueHandler)

  向执行器添加一个可以接收前面执行单元给传递的结果的执行单元，它所执行的线程环境依赖于当前执行单元的线程环境。

* run()

  执行这个线性执行器。

线性执行器通信行用于**线程不定**，**子线程需要等待主线程的结果**，**主线程需要等待子线程的结果**这种情况下，例如：

```java
public class Main{
    public static void main(String[] args){
        Async.createSignleAsync()
            .ui()
            .push(()->{
                //-----------------
                //       1
                //-----------------
                VMLog.error("开始执行");
                VMRuntime.sleep(5000);
            })
            .io()
            .push((IAsyncValue<String>)()->{
                //-----------------
                //       2
                //-----------------
                VMLog.error("创建一个值并向下传递");
                return new Value<>("Hello World!");
            })
            .ui()
            .onValueHandler((IAsyncValueHandler<String>) object ->{
                //-----------------
                //       3
                //-----------------
                VMLog.error("接收到结果：");
                VMLog.error(object.get());
            }
            .push(()->{
                VMLog.error("执行结束");
            })
            .run();
    }
}
```

【控制台输出】

```text
[2019/12/01 17:28:35:600]开始执行
[2019/12/01 17:28:40:600]创建一个值并向下传递
[2019/12/01 17:28:40:601]接收到结果
[2019/12/01 17:28:40:601]执行结束
```

由此可见，无论是在ui还是io线程执行，都是顺序执行，后一个执行器总是会等待当前执行器执行结束。

其中代码块1的执行器会在ui线程中执行，因为在执行代码块之前已经调用ui()方法将执行环境切换至主线程，单纯的执行单元使用Runnable接口类定义，例如：

```java
Runnable run=new Runnable(){
    @Override
    void run(){
        //要执行的内容
    }
};
push(run);
```

代码块2的执行器会在io线程中执行，因为在执行代码块之前已经调用ui()方法将执行环境切换至子线程，同时这里使用了值处理单元，值处理单元由一个接口定义：

```java
public interface IAsyncValue<_Tx> {

    Value<_Tx> onHandle();

}
```

这个接口类接受一个泛型参数，代表可以将任何数据类型的数据传递给下一级执行单元，代码块2中展示了这个的用法，例如：

```java
return Value<String>("Hello World!");
return Value<Number>(10);
return Value<File>(new File("/0/files/null"));
```

如果我要在子线程中读取一个文件的内容并传给TextView，那么可以这样使用：

```java
...
    .io()
    .push(((IAsyncValue<String>)()->{
        File file=new File("text.txt");
        BufferedInputStream in=new BufferedInputStream(new FileInputStream(file));
        ...
        byte[] _ByteArray=in.read(file.length());
        in.close();
        return new Value<>(new String(_ByteArray));
    })
    .ui()
    .push((IAsyncValueHandler<String>)object->{
        TextView textView=findViewById(R.id.textview);  
        textView.setText(object.get());
    })
```

代码块3展示了如何接受上一级执行单元传递的值，接收值执行单元由一个接口类定义：

```java
public interface IAsyncValueHandler<_Tx> {

    void onReceive(Value<_Tx> object);

}
```

这个接口类接受一个泛型参数，代表要接受的对象的具体类型，实现接口的onReceive方法可以获取到这个Value值，在线性执行器中使用了一个Value<_Tx>泛型类型来表示，可以使用这个类的get方法获取实际的值，例如刚刚展示的那个样子：

```java
.push((IAsyncValueHandler<String>)object->{
        TextView textView=findViewById(R.id.textview);  
        textView.setText(object.get());
})
```

通过上面三个代码块向我们展示了线性执行器的三种类型的处理单元：

* 普通执行单元
* 传值执行单元
* 取值执行单元

定义完成整个执行流程后需要调用run方法来执行整个线性执行器。

### 3、无序执行器

无序执行器只能处理普通执行单元，执行的时候会立即根据当前的线程环境开始执行，例如：

```java
public class Main{
    public static void main(String[] args){
        IAsync async=Async.createMultithreadAsync();
    }
}
```

无序执行器由一个接口类定义：

```java
public interface IAsync {

    IAsync io();

    IAsync ui();

    IAsync push(Runnable runnable);
}
```

无序执行器只能执行普通的执行事件，不能进行上下级的传值，因为他不具备顺序性，其他的方法和线性执行器完全一致，例如io()方法切换至子线程，ui()方法切换至主线程，使用push方法执行一个执行单元，例如：

```java
public class Main{
    public static void main(String[] args){
        Async.createMultithreadAsync()
             .io()
             .push(()->{
                 VM.sleep(2000);
             	 VM.error("第一代码块");
             })
             .ui()
             .push(()->{
                 VM.error("第二代码块");       
             })
             .io()
             .push(()->{
             	 VM.error("第三代码块");
             });
    }
}
```

【控制台输出】

```text
[2019/12/01 17:35:28:157]第二代码块
[2019/12/01 17:35:40:157]第三代码块
[2019/12/01 17:35:42:336]第一代码块
```

## 疑问？

1、调用createMultithreadAsync()和createSignleAsync()方法创建执行器后不调用线程切换，直接执行处理单元，那样的线程环境是什么样子的？

```text
如果创建执行器后不进行线程切换直接执行处理单元，那么会直接在当前创建执行器的线程环境中执行。
```

## 开源许可

```text
Copyright 2019 YuQianhao, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

