package com.xhsoft.retrofit.sample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.xhsoft.retrofit.encryption.EncryptionConverterFactory;
import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
import com.xhsoft.retrofit.jsonrpc.JsonRpc;
import com.xhsoft.retrofit.jsonrpc.JsonRpcConverterFactory;
import com.xhsoft.retrofit.jsonrpc.JsonRpcException;
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

/**
 * 演示如何写一个 JsonRpc 同步请求.
 */
public class MultiplicationSyncTest {

  private static final String key = "78165B0FE3319E7E5918968671877DB8F42C48BCC10430FA";

  @Rule
  public final MockWebServer server = new MockWebServer();

  Retrofit retrofit;

  /**
   * setUp.
   */
  @Before
  public void setUp() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(logging).build();

    retrofit = new Retrofit.Builder()
        .client(client)
        .baseUrl(server.url("/")) // Local Server: "http://localhost:1234"
        .addConverterFactory(EncryptionConverterFactory.create(key, key))
        .addConverterFactory(JsonRpcConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(new JsonRpcCallAdapterFactory())
        .build();
  }

  interface MultiplicationService {
    @POST("/rpc")
    @JsonRpc("Arith.multiply")
    JsonRpcCall<Integer> multiply(@Body Object... a);
  }

  @Test
  public void testMultiply() throws Exception {
    server.enqueue(new MockResponse()
        .addHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(EncryptionHelper.encryptText("{"
            + "\"jsonrpc\":\"2.0\","
            + "\"id\":43434343,"
            + "\"result\":6"
            + "}", key, new byte[] {})));

    MultiplicationService service = retrofit.create(MultiplicationService.class);
    try {

      Integer response = service.multiply(2, 3).execute();
      assertThat(response).isEqualTo(6);

    } catch (JsonRpcException ex) {
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
            + "}", key, new byte[] {})));

    MultiplicationService service = retrofit.create(MultiplicationService.class);
    try {

      Integer response = service.multiply(2, 3).execute();
      fail("服务器返回错误时，应该抛出异常");

    } catch (JsonRpcException ex) {
      assertThat(ex.getCode()).isEqualTo(-32603);
      assertThat(ex.getMessage()).isEqualTo("测试错误");
    }
  }
}
