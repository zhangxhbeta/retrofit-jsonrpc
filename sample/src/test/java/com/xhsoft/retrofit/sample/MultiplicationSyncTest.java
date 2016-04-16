package com.xhsoft.retrofit.sample;

import com.xhsoft.retrofit.encryption.EncryptionConverterFactory;
import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
import com.xhsoft.retrofit.jsonrpc.JsonRPC;
import com.xhsoft.retrofit.jsonrpc.JsonRPCConverterFactory;
import com.xhsoft.retrofit.jsonrpc.JsonRPCException;
import com.xhsoft.retrofit.jsonrpc.JsonRPCResponse;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRPCCall;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRPCCallAdapterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MultiplicationSyncTest {

    private static final String ekey = "78165B0FE3319E7E5918968671877DB8F42C48BCC10430FA";
    private static final String dkey = "78165B0FE3319E7E5918968671877DB8F42C48BCC10430FA";

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
                .addConverterFactory(EncryptionConverterFactory.create(ekey, dkey))
                .addConverterFactory(JsonRPCConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(new JsonRPCCallAdapterFactory())
                .build();
    }

    interface MultiplicationService {
        @POST("/rpc")
        @JsonRPC("Arith.multiply")
        JsonRPCCall<Integer> multiply(@Body Object... a);
    }

    @Test
    public void testMultiply() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(EncryptionHelper.encryptText("{"
                        + "\"jsonrpc\":\"2.0\","
                        + "\"id\":43434343,"
                        + "\"result\":6"
                        + "}", dkey, new byte[]{})));

        MultiplicationService service = retrofit.create(MultiplicationService.class);
        try {

            Integer response = service.multiply(2, 3).execute();
            assertThat(response).isEqualTo(6);

        } catch (JsonRPCException e) {
            fail("返回错误的异常");
        }
    }

    @Test
    public void testMultiplyError() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(EncryptionHelper.encryptText("{"
                        + "\"jsonrpc\":\"2.0\","
                        + "\"id\":5141241242,"
                        + "\"error\":{"
                        + "\"code\":-32603,"
                        + "\"message\":\"测试错误\","
                        + "\"data\":\"data\"}"
                        + "}", dkey, new byte[]{})));

        MultiplicationService service = retrofit.create(MultiplicationService.class);
        try {

            Integer response = service.multiply(2, 3).execute();
            fail("服务器返回错误时，应该抛出异常");

        } catch (JsonRPCException e) {
            assertThat(e.getCode()).isEqualTo(-32603);
            assertThat(e.getMessage()).isEqualTo("测试错误");
        }
    }
}
