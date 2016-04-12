package com.segment.jsonrpc;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试如何将 JsonRPC 的错误统一和 Retrofit 的错误统一起来
 * 不然太不方便
 */
public class JsonRPCErrorParseTest {
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
                .build();
    }

    interface MultiplicationService {
        @POST("/rpc")
        @JsonRPC("Arith.multiply")
        Call<Integer> multiply(@Body Object... a);
    }

    @Test
    public void multiply() throws Exception {
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

        MultiplicationService service = retrofit.create(MultiplicationService.class);
        Response<Integer> response = service.multiply(2, 3).execute();

        server.takeRequest();

//        Converter<ResponseBody, JsonRPCResponse<Integer>> errorConverter =
//                retrofit.responseBodyConverter(JsonRPCResponse.class, new Annotation[0]);
//
//        // Convert the error body into our Error type.
//        JsonRPCResponse<Integer> error = errorConverter.convert(response.errorBody());
//        System.out.println("ERROR: " + error.error);

        assertThat(response.body()).isNull();


    }
}
