package com.xhsoft.retrofit.jsonrpc;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

public class JsonRpcConverters {

  static class JsonRpcResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    final Converter<ResponseBody, JsonRpcResponse<T>> delegate;
    final boolean forceReturResponse;

    JsonRpcResponseBodyConverter(Converter<ResponseBody, JsonRpcResponse<T>> delegate,
                                 boolean forceReturResponse) {
      this.delegate = delegate;
      this.forceReturResponse = forceReturResponse;
    }

    @Override
    public T convert(ResponseBody responseBody) throws IOException {
      JsonRpcResponse<T> response = delegate.convert(responseBody);

      if (forceReturResponse) {
        return (T) response;
      } else {
        return response.result;
      }
    }
  }

  static class JsonRpcRequestBodyConverter<T> implements Converter<T, RequestBody> {
    final String method;
    final Converter<JsonRpcRequest, RequestBody> delegate;

    JsonRpcRequestBodyConverter(String method, Converter<JsonRpcRequest, RequestBody> delegate) {
      this.method = method;
      this.delegate = delegate;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      return delegate.convert(JsonRpcRequest.create(method, value));
    }
  }

  static class JsonRpcNotificationBodyConverter<T> implements Converter<T, RequestBody> {
    final String method;
    final Converter<JsonRpcNotification, RequestBody> delegate;

    JsonRpcNotificationBodyConverter(String method,
                                     Converter<JsonRpcNotification, RequestBody> delegate) {
      this.method = method;
      this.delegate = delegate;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      return delegate.convert(JsonRpcNotification.create(method, value));
    }
  }
}
