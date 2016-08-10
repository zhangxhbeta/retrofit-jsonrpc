package com.xhsoft.retrofit.jsonrpc;

/**
 * 按照 JSON-RPC 规范的 Error 定义的异常
 **/
public class JsonRpcException extends RuntimeException {
  private int code;

  public JsonRpcException() {
    super();
    setCode(-32603); // Generic JSON-RPC error code.
  }

  public JsonRpcException(String message) {
    super(message);
    setCode(-32603); // Generic JSON-RPC error code.
  }

  public JsonRpcException(String message, int code) {
    super(message);
    setCode(code);
  }

  public JsonRpcException(JsonRpcError error) {
    super(error.message);
    setCode(error.code);
  }

  public JsonRpcException(Throwable e, int code) {
    super(e.getLocalizedMessage());
    setCode(code);
  }

  /**
   * Set the JSON-RPC error code for this exception
   *
   * @param code
   *            The JSON-RPC error code, usually negative in the range of
   *            -32768 to -32000 inclusive
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * Get the JSON-RPC error code of this exception.
   *
   * @return long Error code, usually negative in the range of -32768 to
   *         -32000 inclusive
   */
  public int getCode() {
    return code;
  }
}
