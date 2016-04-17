package com.xhsoft.retrofit.sample;

import com.xhsoft.retrofit.encryption.EncryptionConverterFactory;
import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
import com.xhsoft.retrofit.jsonrpc.*;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRPCCall;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRPCCallAdapterFactory;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRPCCallback;
import net.jodah.concurrentunit.Waiter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 演示如何写一个 JsonRpc 异步请求
 */
public class MultiplicationAsyncTest {

    private static final String key = "9E7E598F42CB0FE3314830FABC8968671877DB781615C104";

    @Rule
    public final MockWebServer server = new MockWebServer();

    Retrofit retrofit;

    @Before
    public void setUp() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(logging).build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(server.url("/")) // Local Server: "http://localhost:1234"
                .addConverterFactory(EncryptionConverterFactory.create(key, key))
                .addConverterFactory(JsonRPCConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(new JsonRPCCallAdapterFactory())
                .build();
    }

    interface FoobarService {
        @POST("/")
        @JsonRPC
        JsonRPCCall<Integer> jsonRpcCall(@Body Object... a);
    }

    @Test
    public void jsonRpcCallSuccess() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(EncryptionHelper.encryptText("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":4,"
                                + "\"result\":6"
                                + "}", key, new byte[]{})
                ));


        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCall(2, 3).enqueue(new JsonRPCCallback<Integer>() {
            @Override
            public void success(JsonRPCCall<Integer> call, Integer response) {
                waiter.assertEquals(response, 6);
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<Integer> call, JsonRPCError response) {
                waiter.fail("错误调用 error");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<Integer> call, JsonRPCException t) {
                waiter.fail("错误调用 unexpectedError");
                waiter.resume();
            }
        });

        waiter.await();
    }

    @Test
    public void jsonRpcCallUnexpectedError() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(EncryptionHelper.encryptText("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-10086,"
                                + "\"message\":\"测试错误\","
                                + "\"data\":\"data\"}"
                                + "}", key, new byte[]{})
                ));

        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCall(2, 3).enqueue(new JsonRPCCallback<Integer>() {
            @Override
            public void success(JsonRPCCall<Integer> call, Integer response) {
                waiter.fail("错误调用 success");
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<Integer> call, JsonRPCError response) {
                waiter.fail("错误调用 error");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<Integer> call, JsonRPCException t) {
                waiter.assertNotNull(t);
                waiter.assertEquals(t.getCode(), -10086);
                waiter.assertEquals(t.getMessage(), "测试错误");
                waiter.resume();
            }
        });

        waiter.await();
    }

    @Test
    public void jsonRpcCallError() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(EncryptionHelper.encryptText("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-32603,"
                                + "\"message\":\"测试错误\","
                                + "\"data\":\"data\"}"
                                + "}", key, new byte[]{})
                ));

        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCall(2, 3).enqueue(new JsonRPCCallback<Integer>() {
            @Override
            public void success(JsonRPCCall<Integer> call, Integer response) {
                waiter.fail("错误调用 success");
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<Integer> call, JsonRPCError t) {
                waiter.assertNotNull(t);
                waiter.assertEquals(t.getCode(), -32603);
                waiter.assertEquals(t.getMessage(), "测试错误");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<Integer> call, JsonRPCException t) {
                waiter.fail("错误调用 unexpectedError");
                waiter.resume();
            }
        });

        waiter.await();
    }
}
