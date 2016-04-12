package com.segment.jsonrpc;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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

        Type rpcType = Types.newParameterizedType(JsonRPCResponse.class, type);
        Converter<ResponseBody, JsonRPCResponse> delegate =
                retrofit.nextResponseBodyConverter(this, rpcType, annotations);
        //noinspection unchecked
        return new JsonRPCConverters.JsonRPCResponseBodyConverter(delegate);
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
                return new JsonRPCConverters.JsonRPC2NotificationBodyConverter(method, delegate);
            } else {

                Converter<JsonRPCRequest, RequestBody> delegate =
                        retrofit.nextRequestBodyConverter(this, JsonRPCRequest.class, annotations,
                                methodAnnotations);
                //noinspection unchecked
                return new JsonRPCConverters.JsonRPC2RequestBodyConverter(method, delegate);
            }

        }
        return null;
    }

}
