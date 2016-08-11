package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRpcResponse;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

/**
 * JsonRpcCall适配器工厂.
 *
 * @author zhangxh
 */
public class JsonRpcCallAdapterFactory extends CallAdapter.Factory {
  @Override
  public CallAdapter<JsonRpcCall<?>> get(Type returnType, Annotation[] annotations,
                                         Retrofit retrofit) {
    if (getRawType(returnType) != JsonRpcCall.class) {
      return null;
    }

    if (!(returnType instanceof ParameterizedType)) {
      throw new IllegalStateException(
          "JsonRpcCall must have generic type (e.g., JsonRpcCall<ResponseBody>)");
    }

    final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

    final Class<?> t = getRawType(responseType);
    final boolean clientRequireAllResponse = t == JsonRpcResponse.class;
    final Executor callbackExecutor = retrofit.callbackExecutor();

    return new CallAdapter<JsonRpcCall<?>>() {
      @Override
      public Type responseType() {
        return responseType;
      }

      @Override
      public <R> JsonRpcCall<R> adapt(Call<R> call) {
        return new JsonRpcCallAdapter<R>(call, callbackExecutor, clientRequireAllResponse);
      }
    };
  }
}
