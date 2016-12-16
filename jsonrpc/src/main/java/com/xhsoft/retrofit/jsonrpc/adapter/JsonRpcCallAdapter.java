package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRpcError;
import com.xhsoft.retrofit.jsonrpc.JsonRpcException;
import com.xhsoft.retrofit.jsonrpc.JsonRpcResponse;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Adapts a {@link Call} to {@link JsonRpcCall}.
 */
public class JsonRpcCallAdapter<T> implements JsonRpcCall<T> {
  private final Call<T> call;
  private final Executor callbackExecutor;
  private final boolean clientRequireAllResponse;

  JsonRpcCallAdapter(Call<T> call, Executor callbackExecutor, boolean clientRequireAllResponse) {
    this.call = call;
    this.callbackExecutor = callbackExecutor;
    this.clientRequireAllResponse = clientRequireAllResponse;
  }

  private void onResponseProcess(Call<T> call, Response<T> response, JsonRpcCallback<T> callback) {
    int code = response.code();

    if (code >= 200 && code < 300) {
      if (clientRequireAllResponse) {
        JsonRpcResponse res = (JsonRpcResponse) response.body();

        if (res.getError() != null) {
          int errorCode = res.getError().getCode();
          if ((errorCode <= -32000 && errorCode >= -32099) || errorCode == -32603) {
            callback.error(this, res.getError());
          } else {
            callback.unexpectedError(this, new JsonRpcException(res.getError()));
          }
        } else {
          callback.success(this, response.body());
        }

      } else {
        JsonRpcResponse<T> res = (JsonRpcResponse<T>) response.body();
        if (res.getError() != null) {
          int errorCode = res.getError().getCode();
          if ((errorCode <= -32000 && errorCode >= -32099) || errorCode == -32603) {
            callback.error(this, res.getError());
          } else {
            callback.unexpectedError(this, new JsonRpcException(res.getError()));
          }
        } else {
          callback.success(this, res.getResult());
        }
      }

    } else {
      callback.unexpectedError(this, new JsonRpcException("Unexpected response " + response,
          JsonRpcError.ERROR_CODE_NETWORK_ERROR));
    }
  }


  @Override
  public void cancel() {
    call.cancel();
  }

  @Override
  public void enqueue(final JsonRpcCallback<T> callback) {
    call.enqueue(new Callback<T>() {
      @Override
      public void onResponse(final Call<T> call, final Response<T> response) {
        if (callbackExecutor == null) {
          onResponseProcess(call, response, callback);
        } else {
          callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
              onResponseProcess(call, response, callback);
            }
          });
        }
      }

      @Override
      public void onFailure(final Call<T> call, final Throwable t) {
        if (callbackExecutor == null) {
          callback.unexpectedError(JsonRpcCallAdapter.this,
              new JsonRpcException(t, JsonRpcError.ERROR_CODE_NETWORK_ERROR));
        } else {
          callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
              callback.unexpectedError(JsonRpcCallAdapter.this,
                  new JsonRpcException(t, JsonRpcError.ERROR_CODE_NETWORK_ERROR));
            }
          });
        }
      }
    });
  }

  @Override
  public JsonRpcCall<T> clone() {
    return new JsonRpcCallAdapter<T>(call.clone(), callbackExecutor, clientRequireAllResponse);
  }

  @Override
  public T execute() throws JsonRpcException {
    try {
      if (clientRequireAllResponse) {
        T body = call.execute().body();
        JsonRpcResponse res = (JsonRpcResponse) body;
        if (res.getError() != null) {
          throw new JsonRpcException(res.getError());
        }

        return body;
      } else {
        JsonRpcResponse<T> res = (JsonRpcResponse<T>) call.execute().body();
        if (res.getError() != null) {
          throw new JsonRpcException(res.getError());
        }
        return res.getResult();
      }
    } catch (IOException ex) {
      throw new JsonRpcException(ex.getLocalizedMessage(), JsonRpcError.ERROR_CODE_NETWORK_ERROR);
    }
  }

  @Override
  public boolean isExecuted() {
    return call.isExecuted();
  }

  @Override
  public boolean isCanceled() {
    return call.isCanceled();
  }

  @Override
  public Request request() {
    return call.request();
  }
}
