package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRpcError;
import com.xhsoft.retrofit.jsonrpc.JsonRpcException;

/**
 * JsonRpc 回调接口, 接口一般由客户发起异步调用时使用.
 *
 * <pre>
 * {@code
 * FoobarService service = retrofit.create(FoobarService.class);
 *
 * final Waiter waiter = new Waiter();
 * service.jsonRpcCall(2, 3).enqueue(new JsonRpcCallback<Integer>() {
 *
 *   public void success(JsonRpcCall<Integer> call, Integer response) {
 *     waiter.assertEquals(response, 6);
 *     waiter.resume();
 *   }
 *
 *   public void error(JsonRpcCall<Integer> call, JsonRpcError response) {
 *     waiter.fail("错误调用 error");
 *     waiter.resume();
 *   }
 *
 *   public void unexpectedError(JsonRpcCall<Integer> call, JsonRpcException ex) {
 *     waiter.fail("错误调用 unexpectedError");
 *     waiter.resume();
 *   }
 * });
 * }
 * </pre>
 */
public interface JsonRpcCallback<T> {

  /**
   * 调用成功，在返回的 json 里有 result 字段都算成功.
   *
   * @param response 响应成功的内容, 也就是服务接口方法的返回值
   */
  void success(JsonRpcCall<T> call, T response);

  /**
   * 服务器定义错误 -32000 to -32099, 包括默认的 -32603 服务器内部错误.
   * <p/>
   * 一般这种消息必须包含明确的 message，** 客户端用来直接显示给用户 **.
   *
   * @param response 错误内容对象
   */
  void error(JsonRpcCall<T> call, JsonRpcError response);

  /**
   * 其他错误，包括：
   *
   * <pre>
   *   -32603 to -32700 的rpc预定义错误 (除了 -32603 之外)<br>
   *   -32700 服务器接受到了非法 Json，无法解析<br>
   *   -32600 请求对象不符合 JSON2.0 规范<br>
   *   -32601 方法找不到，请检查注解<br>
   *   -32602 方法参数错误<br>
   *   -10001 会话出错<br>
   *   -10004 权限不够<br>
   *   -10002 服务器错误<br>
   *   包括 -10003 网络错误<br>
   *   这种错误，一般由客户端统一拦截处理，如果业务类非要关心，也可以拿到错误对象<br>
   * </pre>
   *
   * @param ex 代表错误的 JsonRpcException 值
   */
  void unexpectedError(JsonRpcCall<T> call, JsonRpcException ex);
}
