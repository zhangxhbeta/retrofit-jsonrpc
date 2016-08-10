package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRpcException;

import okhttp3.Request;

/**
 * jsonrpc 调用.
 */
public interface JsonRpcCall<R> {

  /**
   * 取消当前调用.
   */
  void cancel();

  /**
   * 进入异步队列并执行.
   *
   * @param callback 异步返回执行结果
   */
  void enqueue(JsonRpcCallback<R> callback);

  /**
   * 同步运行，返回结果（会阻塞当前线程）.
   * @return 返回执行结果
   * @throws JsonRpcException JsonRpc 错误封装
   */
  R execute() throws JsonRpcException;

  /**
   * 是否已运行.
   */
  boolean isExecuted();

  /**
   * 是否已取消.
   */
  boolean isCanceled();

  /**
   * 返回底层的 HTTP request 对象.
   */
  Request request();

  /**
   * 复制一份.
   */
  JsonRpcCall<R> clone();
}
