# retrofit-jsonrpc

基于 Retrofit 的 JSON-RPC 2.0 协议实现

## 下载

* 通过标准的 Maven 方式

```xml
<!-- 添加 Jitpack 仓库 -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- 添加依赖 -->
<dependency>
    <groupId>com.github.zhangxhbeta.retrofit-jsonrpc</groupId>
    <artifactId>jsonrpc</artifactId>
    <version>v1.0.3</version>
</dependency>
<dependency>
    <groupId>com.github.zhangxhbeta.retrofit-jsonrpc</groupId>
    <artifactId>encryption</artifactId>
    <version>v1.0.3</version>
</dependency>
```

* Gradle 方式

在根 build.gradle 里面添加
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
添加依赖
```
dependencies {
    compile 'com.github.zhangxhbeta.retrofit-jsonrpc:jsonrpc:v1.0.3'
    compile 'com.github.zhangxhbeta.retrofit-jsonrpc:encryption:v1.0.3'
}
```

## 使用方法

首先声明一下你的模块接口，相比 `Retrofit 2` （注意版本）本来的方式，多了一个注解 `@JsonRpc`，注解的值填写的是 JsonRPC 协议里面的方法名，目前有2个地方不够完美

* 方法的参数声明为 `Object...`，看起来不够强壮，但是我翻遍了 Retrofit 的源码也只能通过 `@Body` 的注解来这么用，希望能找到更好的方式
* 不能直接将方法名附加到协议里面的方法名，这个比较不方便

（如果谁有方法可以联系我或者发个 PR 什么的）

```java
interface MultiplicationService {
    @JsonRpc("Arith.multiply") @POST("rpc")
    JsonRpcCall<Integer> multiply(@Body Object... args);
}
```
通过 Retrofit 注册 `JsonRpcConverterFactory` 和 `JsonRpcCallAdapterFactory`，以及可选的加解密`EncryptionConverterFactory`，注意顺序，加解密如果配置必须是第一个，而接下来就必须是 `JsonRpcCallAdapterFactory`

```java
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:8080/")
        .addConverterFactory(EncryptionConverterFactory.create(ekey, dkey)) // 加密／解密配置（可选）
        .addConverterFactory(JsonRpcConverterFactory.create()) // JsonRpc 转换器，一定要配置
        .addConverterFactory(MoshiConverterFactory.create())   // Json 转换器，也可以选择其他比如 Gson
        .addCallAdapterFactory(new JsonRpcCallAdapterFactory()) // 调用适配器，用于支持 JsonRpcCall，建议加上
        .build();
```

然后通过 Retrofit 创建服务

```java
MultiplicationService service = retrofit.create(MultiplicationService.class);
```

异步方式调用

```java
service.multiply(2, 3).enqueue(new JsonRpcCallback<Integer>() {

    @Override
    public void success(JsonRpcCall<Integer> call, Integer response) {
        int result = response; // -> 6
    }
    
    @Override
    public void error(JsonRpcCall<Integer> call, JsonRpcError response) {
        // 错误处理，注意这里的错误是期待中服务器返回来的，意思是说：
        // 接口解析没问题、网络没问题，业务正常执行，但是服务器返回
        // 错误，提醒客户端哪里有错
    }
    
    @Override
    public void unexpectedError(JsonRpcCall<Integer> call, JsonRpcException t) {
        // 其他错误的处理，注意这个错误是业务未正常执行的错误，包括网络出错、包括接口
        // 配置错误、解析错误
        // 另外如果没有权限、没有登录等错误也都在这里统一处理
    }
});
```

同步方式调用

```java
int result = service.multiply(2, 3).execute(); // -> 6
```

## Todo 清单

- [x] 完整的 `JSON-RPC 2.0` 规范实现
- [x] `JsonRpcCall` 适配器，替代默认的 `Call`
- [x] `http body` 传输中加密/解密（基于3DES/base64)
- [ ] 强类型的接口方法参数声明，替代 `Object...`
- [ ] 支持直接使用 `Java` 方法名
- [x] 单向加密解密开关
- [ ] 安卓环境测试
- [ ] 补充更多例子，如：错误统一拦截处理，附加自定义 http 头，Cookie 处理等



## 感谢

很多借鉴了 [retrofit-jsonrpc](https://github.com/segmentio/retrofit-jsonrpc)，以及 [retrofit-jsonrpc](https://github.com/Tolriq/retrofit-jsonrpc)，thank you.
