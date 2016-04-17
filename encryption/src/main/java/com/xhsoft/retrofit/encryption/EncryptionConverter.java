package com.xhsoft.retrofit.encryption;

import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * 加解密转换器
 *
 * Created by zhangxh on 16/4/16.
 */
public class EncryptionConverter {

    private static final byte[] BYTE_I_V = {};

    /**
     * 解密服务器的响应
     */
    static class EncryptionResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        final Converter<ResponseBody, T> delegate;

        private String key;

        public EncryptionResponseBodyConverter(Converter<ResponseBody, T> delegate, String key) {
            this.key = key;
            this.delegate = delegate;
        }

        @Override
        public T convert(ResponseBody responseBody) throws IOException {
            try {
                byte[] d = EncryptionHelper.dencryptBytes(responseBody.bytes(), key, BYTE_I_V);
                ResponseBody body = ResponseBody.create(responseBody.contentType(), d);
                return delegate.convert(body);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 加密后发送到对方
     *
     * @param <T>
     */
    static class EncryptionRequestBodyConverter<T> implements Converter<T, RequestBody> {
        final Converter<T, RequestBody> delegate;

        private String key;

        public EncryptionRequestBodyConverter(Converter<T, RequestBody> delegate, String key) {
            this.key = key;
            this.delegate = delegate;
        }

        @Override
        public RequestBody convert(T object) throws IOException {
            try {
                RequestBody body = delegate.convert(object);

                Buffer buffer = new Buffer();
                body.writeTo(buffer);

                byte[] d = EncryptionHelper.encryptBytes(buffer.readByteArray(), key, BYTE_I_V);

                return RequestBody.create(body.contentType(), d);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
