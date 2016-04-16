package com.segment.jsonrpc;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import java.io.IOException;

public class JsonRPCConverters {

    static class JsonRPCResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        final Converter<ResponseBody, JsonRPCResponse<T>> delegate;
        final boolean forceReturResponse;

        JsonRPCResponseBodyConverter(Converter<ResponseBody, JsonRPCResponse<T>> delegate, boolean forceReturResponse) {
            this.delegate = delegate;
            this.forceReturResponse = forceReturResponse;
        }

        @Override
        public T convert(ResponseBody responseBody) throws IOException {
            JsonRPCResponse<T> response = delegate.convert(responseBody);

            if (forceReturResponse) {
                return (T) response;
            } else {
                return response.result;
            }
        }
    }

    static class JsonRPCRequestBodyConverter<T> implements Converter<T, RequestBody> {
        final String method;
        final Converter<JsonRPCRequest, RequestBody> delegate;

        JsonRPCRequestBodyConverter(String method, Converter<JsonRPCRequest, RequestBody> delegate) {
            this.method = method;
            this.delegate = delegate;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            return delegate.convert(JsonRPCRequest.create(method, value));
        }
    }

    static class JsonRPCNotificationBodyConverter<T> implements Converter<T, RequestBody> {
        final String method;
        final Converter<JsonRPCNotification, RequestBody> delegate;

        JsonRPCNotificationBodyConverter(String method, Converter<JsonRPCNotification, RequestBody> delegate) {
            this.method = method;
            this.delegate = delegate;
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            return delegate.convert(JsonRPCNotification.create(method, value));
        }
    }
}
