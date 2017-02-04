package com.xhsoft.retrofit.sample;

import com.xhsoft.retrofit.encryption.EncryptionConverterFactory;
import com.xhsoft.retrofit.jsonrpc.JsonRpc;
import com.xhsoft.retrofit.jsonrpc.JsonRpcConverterFactory;
import com.xhsoft.retrofit.jsonrpc.JsonRpcError;
import com.xhsoft.retrofit.jsonrpc.JsonRpcException;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCall;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCallAdapterFactory;
import com.xhsoft.retrofit.jsonrpc.adapter.JsonRpcCallback;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author zhangxh
 */
public class LoginAsync {

  private static final String rpcEndpoint = "http://10.7.1.79:8080/";

  private static final String key = "78165B0FE3319E7E5918968671877DB8F42C48BCC10430FA";

  Retrofit retrofit;

  public static void main(String[] args) {
    LoginAsync test = new LoginAsync();
    test.setUp();

    try {
      test.jsonRpcCallSuccess();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * setup.
   */
  public void setUp() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(logging).build();

    retrofit = new Retrofit.Builder()
        .client(client)
        .baseUrl(rpcEndpoint) // Local Server: "http://localhost:1234"
        .addConverterFactory(EncryptionConverterFactory.create(key, key))
        .addConverterFactory(JsonRpcConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(new JsonRpcCallAdapterFactory())
        .build();
  }

  static class LoginResult {
    private LoginResponse loginResponse;

    public LoginResponse getLoginResponse() {
      return loginResponse;
    }

    public void setLoginResponse(LoginResponse loginResponse) {
      this.loginResponse = loginResponse;
    }
  }

  static class LoginResponse {
    private String sk_username;
    private String sk_userlogo;
    private String sk_realname;
    private String sk_userid;
    private String sk_depid;
    private String sk_depname;
    private String sk_mobile;

    public String getSk_username() {
      return sk_username;
    }

    public void setSk_username(String sk_username) {
      this.sk_username = sk_username;
    }

    public String getSk_userlogo() {
      return sk_userlogo;
    }

    public void setSk_userlogo(String sk_userlogo) {
      this.sk_userlogo = sk_userlogo;
    }

    public String getSk_realname() {
      return sk_realname;
    }

    public void setSk_realname(String sk_realname) {
      this.sk_realname = sk_realname;
    }

    public String getSk_userid() {
      return sk_userid;
    }

    public void setSk_userid(String sk_userid) {
      this.sk_userid = sk_userid;
    }

    public String getSk_depid() {
      return sk_depid;
    }

    public void setSk_depid(String sk_depid) {
      this.sk_depid = sk_depid;
    }

    public String getSk_depname() {
      return sk_depname;
    }

    public void setSk_depname(String sk_depname) {
      this.sk_depname = sk_depname;
    }

    public String getSk_mobile() {
      return sk_mobile;
    }

    public void setSk_mobile(String sk_mobile) {
      this.sk_mobile = sk_mobile;
    }
  }

  interface Login {
    @POST("/oa/JSON-RPC")
    @JsonRpc("user.login")
    JsonRpcCall<LoginResult> login(@Body Object... a);
  }

  public void jsonRpcCallSuccess() throws Exception {
    Login service = retrofit.create(Login.class);


    service.login("admin", "123456", "", "deviceId", "idid").enqueue(new JsonRpcCallback<LoginResult>() {
      @Override
      public void success(JsonRpcCall<LoginResult> call, LoginResult response) {
        System.out.println(response.loginResponse.getSk_depname());
        System.out.println(response.loginResponse.getSk_realname());
        System.out.println(response.loginResponse.getSk_mobile());
      }

      @Override
      public void error(JsonRpcCall<LoginResult> call, JsonRpcError response) {
        System.out.println("错误调用 error");
        System.out.println(response.getCode() + ":" +  response.getMessage());
      }

      @Override
      public void unexpectedError(JsonRpcCall<LoginResult> call, JsonRpcException ex) {
        System.out.println("错误调用 unexpectedError");
        ex.printStackTrace();
      }
    });
  }
}
