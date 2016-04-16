package com.xhsoft.jsonrpc.adapter;

import com.xhsoft.jsonrpc.JsonRPCError;
import com.xhsoft.jsonrpc.JsonRPCException;

/**
 * JsonRPC 回调
 * @param <T>
 */
public interface JsonRPCCallback<T> {
    /**
     * 调用成功，在返回的 json 里有 result 字段都算成功
     * @param response
     */
    void success(JsonRPCCall<T> call, T response);

    /**
     * 服务器定义错误 -32000 to -32099
     * 包括默认的 -32603 服务器内部错误
     * 一般这种消息必须包含明确的 message，** 客户端用来直接显示给用户 **
     * @param response
     */
    void error(JsonRPCCall<T> call, JsonRPCError response);

    /**
     * 其他错误，包括：-32603 to -32700 的rpc预定义错误（除了 -32603 之外）
     * -32700	服务器接受到了非法 Json，无法解析
     * -32600	请求对象不符合 JSON2.0 规范
     * -32601	方法找不到，请检查注解
     * -32602	方法参数错误
     * 包括 -10001 会话出错
     * -10004 权限不够
     * -10002 服务器错误
     * 包括 -10003 网络错误
     * 这种错误，一般由客户端统一拦截处理，如果业务类非要关心，也可以拿到错误对象
     * @param t
     */
    void unexpectedError(JsonRPCCall<T> call, JsonRPCException t);
}
