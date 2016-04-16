package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRPCResponse;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

/**
 * Created by zhangxh on 16/4/14.
 */
public class JsonRPCCallAdapterFactory extends CallAdapter.Factory {
    @Override
    public CallAdapter<JsonRPCCall<?>> get(Type returnType, Annotation[] annotations,
                                           Retrofit retrofit) {
        if (getRawType(returnType) != JsonRPCCall.class) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "JsonRPCCall must have generic type (e.g., JsonRPCCall<ResponseBody>)");
        }

        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

        final Class<?> t = getRawType(responseType);
        final boolean clientRequireAllResponse = t == JsonRPCResponse.class;
        final Executor callbackExecutor = retrofit.callbackExecutor();

        return new CallAdapter<JsonRPCCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public <R> JsonRPCCall<R> adapt(Call<R> call) {
                return new JsonRPCCallAdapter<R>(call, callbackExecutor, clientRequireAllResponse);
            }
        };
    }
}
