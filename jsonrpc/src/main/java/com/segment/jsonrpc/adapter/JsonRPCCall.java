package com.segment.jsonrpc.adapter;

import com.segment.jsonrpc.JsonRPCException;
import okhttp3.Request;

/**
 * jsonrpc 调用
 */
public interface JsonRPCCall<R> {

    /**
     * 取消当前调用
     */
    void cancel();

    /**
     * 进入异步队列并执行
     * @param callback 异步返回执行结果
     */
    void enqueue(JsonRPCCallback<R> callback);

    /**
     * 同步运行，返回结果（会阻塞当前线程）
     * @return
     * @throws JsonRPCException
     */
    R execute() throws JsonRPCException;

    /**
     * 是否已运行
     * @return
     */
    boolean isExecuted();

    /**
     * 是否已取消
     * @return
     */
    boolean isCanceled();

    /** 返回底层的 HTTP request 对象 */
    Request request();

    /**
     * 复制一份
     * @return
     */
    JsonRPCCall<R> clone();
}
