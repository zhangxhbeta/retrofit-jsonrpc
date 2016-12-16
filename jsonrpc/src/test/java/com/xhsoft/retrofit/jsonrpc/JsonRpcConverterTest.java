package com.xhsoft.retrofit.jsonrpc;

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

import static org.assertj.core.api.Assertions.assertThat;

public class JsonRpcConverterTest {
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
                .build();
    }

    interface MultiplicationService {
        @POST("/rpc")
        @JsonRpc("Arith.multiply")
        Call<Integer> multiply(@Body Object... a);

        @POST("/rpc")
        @JsonRpc("Arith.multiply")
        Call<JsonRpcResponse<Integer>> getResponseMultiply(@Body Object... a);
    }

    @Test
    public void multiply() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":4,"
                                + "\"result\":6"
                                + "}"
                ));

        MultiplicationService service = retrofit.create(MultiplicationService.class);

        Response<Integer> response = service.multiply(2, 3).execute();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeader("Content-Type"))
                .isEqualToIgnoringCase("application/json; charset=utf-8");
        assertThat(request.getBody().readByteString().utf8().replaceAll("\"id\":[0-9]+,", "")) //
                .isEqualTo("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"method\":\"Arith.multiply\","
                                + "\"params\":[2,3]"
                                + "}"
                );


        assertThat(response.body()).isEqualTo(6);
    }

    @Test
    public void getResponseMultiply() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"id\":4,"
                                + "\"result\":6"
                                + "}"
                ));

        MultiplicationService service = retrofit.create(MultiplicationService.class);

        Response<JsonRpcResponse<Integer>> response = service.getResponseMultiply(2, 3).execute();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeader("Content-Type"))
                .isEqualToIgnoringCase("application/json; charset=utf-8");
        assertThat(request.getBody().readByteString().utf8().replaceAll("\"id\":[0-9]+,", "")) //
                .isEqualTo("{"
                                + "\"jsonrpc\":\"2.0\","
                                + "\"method\":\"Arith.multiply\","
                                + "\"params\":[2,3]"
                                + "}"
                );

        assertThat(response.body().getResult()).isEqualTo(6);
    }
}
