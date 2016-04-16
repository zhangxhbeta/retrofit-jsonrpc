package com.segment.jsonrpc;

import com.segment.jsonrpc.adapter.JsonRPCCall;
import com.segment.jsonrpc.adapter.JsonRPCCallAdapterFactory;
import com.segment.jsonrpc.adapter.JsonRPCCallback;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * 测试通过 JsonRPC 适配器来搞定无法直接返回body的问题
 */
public class JsonRPCCallAsyncTest {
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
                .addConverterFactory(JsonRPCConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(new JsonRPCCallAdapterFactory())
                .build();
    }

    interface FoobarService {
        @POST("/")
        @JsonRPC
        JsonRPCCall<Integer> jsonRpcCall(@Body Object... a);

        @POST("/")
        @JsonRPC
        JsonRPCCall<JsonRPCResponse<Integer>> jsonRpcCallResponse(@Body Object... a);
    }

    @Test
    public void jsonRpcCallSuccess() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":4,"
                                + "\"result\":6"
                                + "}"
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
    public void jsonRpcCallResponseSuccess() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":4,"
                                + "\"result\":6"
                                + "}"
                ));

        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCallResponse(2, 3).enqueue(new JsonRPCCallback<JsonRPCResponse<Integer>>() {
            @Override
            public void success(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCResponse<Integer> response) {
                waiter.assertEquals(response.getResult(), 6);
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCError response) {
                waiter.fail("错误调用 error");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCException t) {
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
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-10086,"
                                + "\"message\":\"测试错误\","
                                + "\"data\":\"data\"}"
                                + "}"
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
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-32603,"
                                + "\"message\":\"测试错误\","
                                + "\"data\":\"data\"}"
                                + "}"
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

    @Test
    public void jsonRpcCallResponseError() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-32603,"
                                + "\"message\":\"测试错误\","
                                + "\"data\":\"data\"}"
                                + "}"
                ));

        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCallResponse(2, 3).enqueue(new JsonRPCCallback<JsonRPCResponse<Integer>>() {
            @Override
            public void success(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCResponse<Integer> response) {
                waiter.fail("错误调用 success");
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCError response) {
                waiter.assertNotNull(response);
                waiter.assertEquals(response.getCode(), -32603);
                waiter.assertEquals(response.getMessage(), "测试错误");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCException t) {
                waiter.fail("错误调用 unexpectedError");
                waiter.resume();
            }
        });

        waiter.await();
    }

    @Test
    public void jsonRpcCallResponseUnpectedError() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":5,"
                                + "\"error\":{"
                                + "\"code\":-32700,"
                                + "\"message\":\"解析错误\","
                                + "\"data\":\"data\"}"
                                + "}"
                ));

        FoobarService service = retrofit.create(FoobarService.class);

        final Waiter waiter = new Waiter();
        service.jsonRpcCallResponse(2, 3).enqueue(new JsonRPCCallback<JsonRPCResponse<Integer>>() {
            @Override
            public void success(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCResponse<Integer> response) {
                waiter.fail("错误调用 success");
                waiter.resume();
            }

            @Override
            public void error(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCError response) {
                waiter.fail("错误调用 error");
                waiter.resume();
            }

            @Override
            public void unexpectedError(JsonRPCCall<JsonRPCResponse<Integer>> call, JsonRPCException t) {
                waiter.assertNotNull(t);
                waiter.assertEquals(t.getCode(), -32700);
                waiter.assertEquals(t.getMessage(), "解析错误");
                waiter.resume();
            }
        });

        waiter.await();
    }
}
