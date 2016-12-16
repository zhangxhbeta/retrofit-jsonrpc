package com.xhsoft.retrofit.jsonrpc;

import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCall;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCallAdapterFactory;

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
 * 测试通过 JsonRpc 适配器来搞定无法直接返回body的问题
 */
public class JsonRpcCallTest {
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
                .addConverterFactory(JsonRpcConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(new JsonRpcCallAdapterFactory())
                .build();
    }

    interface FoobarService {
        @POST("/")
        @JsonRpc
        JsonRpcCall<Integer> jsonRpcCall(@Body Object... a);

        @POST("/")
        @JsonRpc
        JsonRpcCall<JsonRpcResponse<Integer>> jsonRpcCallResponse(@Body Object... a);
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
        Integer response = service.jsonRpcCall(2, 3).execute();

        assertThat(response).isEqualTo(6);
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
        JsonRpcResponse<Integer> response = service.jsonRpcCallResponse(2, 3).execute();

        assertThat(response.getResult()).isEqualTo(6);
    }

    @Test
    public void jsonRpcCallError() throws Exception {
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

        try {
            service.jsonRpcCall(2, 3).execute();
            fail("失败未抛出异常");
        } catch (JsonRpcException e) {
            assertThat(e.getCode()).isEqualTo(-10086);
        }

    }

    @Test
    public void jsonRpcCallResponseError() throws Exception {
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

        try {
            JsonRpcResponse<Integer> res = service.jsonRpcCallResponse(2, 3).execute();
            assertThat(res.getResult()).isNull();
            assertThat(res.getError().getCode()).isEqualTo(-10086);
            fail("失败未抛出异常");
        } catch (JsonRpcException e) {
            assertThat(e.getCode()).isEqualTo(-10086);
        }

    }
}
