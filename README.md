![](https://img.shields.io/badge/build-passing-green.svg)&nbsp;![](https://img.shields.io/badge/release-v0.2.0-green.svg)&nbsp;![](https://img.shields.io/badge/JCenter-0.2.0-brightgreen.svg)&nbsp;![](https://img.shields.io/badge/license-Apache2-blue.svg)

<br>

Dor 是一个用于快速构建Android 的网络请求库，它是基于当前最火的网络请求框架 Retrofit2+RxJava2 ，在此基础上封装了一些最常用的基础配置，并提供了更加强大的功能。



<br>

### 特性

Retrofit2 的最大特点就是利用接口的动态代理技术，把我们所有的 API 都放在一处进行统一管理，并且通过接口就可以完成一个网络接口的所需配置。所有，在 Dor 中，这个特性得到保留。

Dor 在对 Retrofit2 + RxJava2 进行封装时，不仅仅封装了一些常用的基础配置，而且还最大程度的保留了其灵活性，如果不满意默认的封装，你还可以进行定制化。

* 默认使用 Gson解析，支持解析为 String。
* 支持文件的下载、进度监听、断点续传、多线程下载。
* 支持 Cookie 的持久化，并可灵活操作 Cookie
* 日志管理
* 网络状态管理
* 统一结果回调


<br>


### 配置

在项目的 build.gradle 中添加如下依赖：

```groovy
compile 'com.deemons.dor:dor:x.x.x'
```

<br>

### 使用

1. 首先， Dor 需要在使用前进行初始化。

   ```java
   Dor.init(context);

   //或者，需要进行一些定制化配置
    Dor.init(Dor.builder()
             .context(this)
             .baseUrl(API.HOST)
             .logLevel(HttpLoggingInterceptor.Level.BODY)
            );
   ```

2. 之后的使用就与 Retrofit2 的使用方式一样，编写接口

   ```java
   public interface API {
       String HOST = "http://gank.io/api/";

       /**
        * 技术文章列表
        */
       @GET("data/{tech}/{num}/{page}")
       Observable<String> getTechList(
         @Path("tech") String tech, 
         @Path("num") int num, 
         @Path("page") int page
       );
   }
   ```

   然后，获取实例后，直接调用。

   ```java
           API api = Dor.getAPI(API.class);

           api.getGirlList(8, 8)
                   .compose(RxUtils.<String>io_main())
                   .subscribe(new DorObserver<String>() {
                       @Override
                       public void onNext(@NonNull String s) {

                       }
                   });
   ```

如果目前还对Retrofit2 的使用方式不熟悉的话，这里有一篇文章 [Retrofit2 的简单使用](http://blog.csdn.net/deemons/article/details/78187965) 。



如果你想下载文件：

```java
        Dor.download(downloadUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DorObserver<Status>() {
                    @Override
                    public void onNext(@NonNull Status s) {

                    }
                });
```



### 说明

目前，此项目功能尚未稳定，并且功能还在持续增加完善中。