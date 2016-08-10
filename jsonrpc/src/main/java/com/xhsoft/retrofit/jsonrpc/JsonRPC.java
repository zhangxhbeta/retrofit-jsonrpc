package com.xhsoft.retrofit.jsonrpc;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * JSON-RPC 2.0 请求
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface JsonRpc {
  /**
   * 调用方法，对应到 JsonRpc 规范里面的 method
   */
  String value() default "";

  /**
   * 将 Java 方法名追加到 method。目前暂不支持
   */
  boolean useJavaMethodName() default false;

  /**
   * 规范里面定义的，没有id的特殊请求，不需要有响应
   */
  boolean notification() default false;
}
