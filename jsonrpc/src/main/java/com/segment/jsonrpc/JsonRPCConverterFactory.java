package com.segment.jsonrpc;

import com.segment.jsonrpc.adapter.JsonRPCCall;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonRPCConverterFactory extends Converter.Factory {
    public static JsonRPCConverterFactory create() {
        return new JsonRPCConverterFactory();
    }

    private JsonRPCConverterFactory() {
        // Private constructor.
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                            Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (!Utils.isAnnotationPresent(annotations, JsonRPC.class)
                && !Utils.isAnnotationPresent(annotations, JsonRPC.class)) {
            return null;
        }

        final Class<?> t = Utils.getRawType(type);

        // 调用方期待返回整个协议体，无论是 Call 还是 JsonRPCCall 都可以很好支持
        if (t == JsonRPCResponse.class) {
            Converter<ResponseBody, JsonRPCResponse> delegate =
                    retrofit.nextResponseBodyConverter(this, type, annotations);

            return delegate;
        }

        // 调用方只要求返回结果，这里有2种情况
        // 1、配置了Adapter，这时候可以返回其他信息给 JsonRPCAdapter
        // 2、没有配置，那么就只能返回正确响应了，错误被丢失

        Type rpcType = Types.newParameterizedType(JsonRPCResponse.class, type);
        Type returnType = Types.newParameterizedType(JsonRPCCall.class, rpcType);
        Converter<ResponseBody, JsonRPCResponse> delegate =
                retrofit.nextResponseBodyConverter(this, rpcType, annotations);
        try {
            CallAdapter<?> adapter = retrofit.callAdapter(returnType, annotations);
            // 有适配器
            return new JsonRPCConverters.JsonRPCResponseBodyConverter(delegate, true);
        } catch (IllegalArgumentException e) {
            // 这种情况下说明没有配置适配器
            return new JsonRPCConverters.JsonRPCResponseBodyConverter(delegate, false);
        }
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations,
                                                          Annotation[] methodAnnotations, Retrofit retrofit) {
        JsonRPC jsonRPCAnnotation = Utils.findAnnotation(methodAnnotations, JsonRPC.class);
        if (jsonRPCAnnotation != null) {
            String method = jsonRPCAnnotation.value();
            boolean notification = jsonRPCAnnotation.notification();

            if (notification) {

                Converter<JsonRPCNotification, RequestBody> delegate =
                        retrofit.nextRequestBodyConverter(this, JsonRPCNotification.class, annotations,
                                methodAnnotations);
                //noinspection unchecked
                return new JsonRPCConverters.JsonRPCNotificationBodyConverter(method, delegate);
            } else {

                Converter<JsonRPCRequest, RequestBody> delegate =
                        retrofit.nextRequestBodyConverter(this, JsonRPCRequest.class, annotations,
                                methodAnnotations);
                //noinspection unchecked
                return new JsonRPCConverters.JsonRPCRequestBodyConverter(method, delegate);
            }

        }
        return null;
    }

}
