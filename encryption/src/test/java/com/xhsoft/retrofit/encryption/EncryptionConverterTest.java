package com.xhsoft.retrofit.encryption;

import com.xhsoft.retrofit.encryption.tools.EncryptionHelper;
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

public class EncryptionConverterTest {

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
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }

    interface MultiplicationService {
        @POST("/rpc")
        Call<SampleResponse> echo(@Body SampleRequest a);
    }

    static class SampleRequest {
        String foo;

        public SampleRequest(String foo) {
            this.foo = foo;
        }
    }

    static class SampleResponse {
        String bar;

        public SampleResponse(String bar) {
            this.bar = bar;
        }
    }

    @Test
    public void testEncryptionCall() throws Exception {

        String requestBody = "{\"foo\":\"bar\"}";
        String responseBody = "{\"bar\":\"foo\"}";

        String requestBodyEncryption = EncryptionHelper.encryptText(requestBody, ekey, new byte[]{});
        String responseBodyEncryption = EncryptionHelper.encryptText(responseBody, dkey, new byte[]{});

        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(responseBodyEncryption));

        MultiplicationService service = retrofit.create(MultiplicationService.class);

        Response<SampleResponse> response = service.echo(new SampleRequest("bar")).execute();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeader("Content-Type"))
                .isEqualToIgnoringCase("application/json; charset=utf-8");

        byte[] bs = request.getBody().readByteArray();
        assertThat(new String(bs)).isEqualTo(requestBodyEncryption);


        assertThat(response.body().bar).isEqualTo("foo");
    }
}
