package com.xhsoft.retrofit.jsonrpc;

import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCall;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 支持 Retrofit 的 JsonRpc 转换器
 */
public class JsonRpcConverterFactory extends Converter.Factory {
  public static JsonRpcConverterFactory create() {
    return new JsonRpcConverterFactory();
  }

  private JsonRpcConverterFactory() {
    // Private constructor.
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                          Annotation[] annotations,
                                                          Retrofit retrofit) {
    if (!Utils.isAnnotationPresent(annotations, JsonRpc.class)
        && !Utils.isAnnotationPresent(annotations, JsonRpc.class)) {
      return null;
    }

    final Class<?> t = Utils.getRawType(type);

    // 调用方期待返回整个协议体，无论是 Call 还是 JsonRpcCall 都可以很好支持
    if (t == JsonRpcResponse.class) {
      return retrofit.nextResponseBodyConverter(this, type, annotations);
    }

    // 调用方只要求返回结果，这里有2种情况
    // 1、配置了Adapter，这时候可以返回其他信息给 JsonRPCAdapter
    // 2、没有配置，那么就只能返回正确响应了，错误被丢失

    Type rpcType = Types.newParameterizedType(JsonRpcResponse.class, type);
    Type returnType = Types.newParameterizedType(JsonRpcCall.class, rpcType);
    Converter<ResponseBody, JsonRpcResponse> delegate =
        retrofit.nextResponseBodyConverter(this, rpcType, annotations);
    try {
      CallAdapter<?> adapter = retrofit.callAdapter(returnType, annotations);
      // 有适配器
      return new JsonRpcConverters.JsonRpcResponseBodyConverter(delegate, true);
    } catch (IllegalArgumentException ex) {
      // 这种情况下说明没有配置适配器
      return new JsonRpcConverters.JsonRpcResponseBodyConverter(delegate, false);
    }
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(
      Type type, Annotation[] annotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    JsonRpc jsonRpcAnnotation = Utils.findAnnotation(methodAnnotations, JsonRpc.class);
    if (jsonRpcAnnotation != null) {
      String method = jsonRpcAnnotation.value();
      boolean notification = jsonRpcAnnotation.notification();

      if (notification) {

        Converter<JsonRpcNotification, RequestBody> delegate =
            retrofit.nextRequestBodyConverter(this, JsonRpcNotification.class, annotations,
                methodAnnotations);
        //noinspection unchecked
        return new JsonRpcConverters.JsonRpcNotificationBodyConverter(method, delegate);
      } else {

        Converter<JsonRpcRequest, RequestBody> delegate =
            retrofit.nextRequestBodyConverter(this, JsonRpcRequest.class, annotations,
                methodAnnotations);
        //noinspection unchecked
        return new JsonRpcConverters.JsonRpcRequestBodyConverter(method, delegate);
      }

    }
    return null;
  }

}
