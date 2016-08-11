package com.xhsoft.retrofit.jsonrpc;

/**
 * JsonRpc 错误.
 *
 * @author zhangxh
 */
public class JsonRpcError {
  /**
   * 解析错误.
   */
  public static final int PREDEFINED_ERROR_PARSE_ERROR = -32700;

  /**
   * 不合适的请求.
   */
  public static final int PREDEFINED_ERROR_INVALID_REQUEST = -32600;

  /**
   * 方法未找到.
   */
  public static final int PREDEFINED_ERROR_METHOD_NOT_FOUND = -32601;

  /**
   * 不合适的参数.
   */
  public static final int PREDEFINED_ERROR_INVALID_PARAMS = -32602;

  /**
   * 内部错误.
   */
  public static final int PREDEFINED_ERROR_INTERNAL_ERROR = -32603;

  /**
   * 扩展错误：非法会话，未登录或没有权限.
   */
  public static final int ERROR_CODE_INVALID_SESSION = -10001;

  /**
   * 扩展错误：远程服务器错误.
   */
  public static final int ERROR_CODE_REMOTE_ERROR = -10002;

  /**
   * 扩展错误：网络错误（客户端使用）.
   */
  public static final int ERROR_CODE_NETWORK_ERROR = -10003;

  /**
   * 扩展错误： 已登录认证但是没有相应权限.
   */
  public static final int ERROR_CODE_AUTHZ_ERROR = -10004;

  Integer code;
  String message;
  Object data;

  public Integer getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public Object getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JsonRpcError that = (JsonRpcError) o;

    if (!code.equals(that.code)) {
      return false;
    }
    return message.equals(that.message);

  }

  @Override
  public int hashCode() {
    int result = code.hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "JsonRpcError{"
        + "message='" + message + '\''
        + ", code=" + code
        + '}';
  }
}
