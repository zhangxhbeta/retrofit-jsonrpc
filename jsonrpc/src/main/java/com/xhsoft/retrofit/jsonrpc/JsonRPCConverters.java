package com.xhsoft.retrofit.jsonrpc;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

public class JsonRpcConverters {

  static class JsonRPCResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    final Converter<ResponseBody, JsonRpcResponse<T>> delegate;
    final boolean forceReturResponse;

    JsonRPCResponseBodyConverter(Converter<ResponseBody, JsonRpcResponse<T>> delegate, boolean forceReturResponse) {
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

  static class JsonRPCRequestBodyConverter<T> implements Converter<T, RequestBody> {
    final String method;
    final Converter<JsonRpcRequest, RequestBody> delegate;

    JsonRPCRequestBodyConverter(String method, Converter<JsonRpcRequest, RequestBody> delegate) {
      this.method = method;
      this.delegate = delegate;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      return delegate.convert(JsonRpcRequest.create(method, value));
    }
  }

  static class JsonRPCNotificationBodyConverter<T> implements Converter<T, RequestBody> {
    final String method;
    final Converter<JsonRpcNotification, RequestBody> delegate;

    JsonRPCNotificationBodyConverter(String method, Converter<JsonRpcNotification, RequestBody> delegate) {
      this.method = method;
      this.delegate = delegate;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      return delegate.convert(JsonRpcNotification.create(method, value));
    }
  }
}
