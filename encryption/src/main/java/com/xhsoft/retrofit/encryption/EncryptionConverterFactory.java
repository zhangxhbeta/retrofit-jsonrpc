package com.xhsoft.retrofit.encryption;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 加密／解密的转换库
 *
 * Created by zhangxh on 16/4/16.
 */
public class EncryptionConverterFactory extends Converter.Factory {

    private String encryptionKey;

    private String dencryptionKey;

    public static EncryptionConverterFactory create(String encryptionKey, String dencryptionKey) {
        return new EncryptionConverterFactory(encryptionKey, dencryptionKey);
    }

    public static EncryptionConverterFactory create(String encryptionKey) {
        return new EncryptionConverterFactory(encryptionKey, encryptionKey);
    }

    private EncryptionConverterFactory(String encryptionKey, String dencryptionKey) {
        // Private constructor.
        this.encryptionKey = encryptionKey;
        this.dencryptionKey = dencryptionKey;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Converter<ResponseBody, ?> delegate =
                retrofit.nextResponseBodyConverter(this, type, annotations);

        return new EncryptionConverter.EncryptionResponseBodyConverter(delegate, dencryptionKey);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        Converter<?, RequestBody> delegate =
                retrofit.nextRequestBodyConverter(this, type, parameterAnnotations,
                        methodAnnotations);

        return new EncryptionConverter.EncryptionRequestBodyConverter(delegate, encryptionKey);
    }
}
