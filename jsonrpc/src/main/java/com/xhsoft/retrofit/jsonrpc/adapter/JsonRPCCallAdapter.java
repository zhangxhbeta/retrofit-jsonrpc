package com.xhsoft.retrofit.jsonrpc.adapter;

import com.xhsoft.retrofit.jsonrpc.JsonRPCError;
import com.xhsoft.retrofit.jsonrpc.JsonRPCException;
import com.xhsoft.retrofit.jsonrpc.JsonRPCResponse;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Adapts a {@link Call} to {@link JsonRPCCall}.
 */
public class JsonRPCCallAdapter<T> implements JsonRPCCall<T> {
    private final Call<T> call;
    private final Executor callbackExecutor;
    private final boolean clientRequireAllResponse;

    JsonRPCCallAdapter(Call<T> call, Executor callbackExecutor, boolean clientRequireAllResponse) {
        this.call = call;
        this.callbackExecutor = callbackExecutor;
        this.clientRequireAllResponse = clientRequireAllResponse;
    }

    public void _onResponse(Call<T> call, Response<T> response, JsonRPCCallback<T> callback) {
        int code = response.code();

        if (code >= 200 && code < 300) {
            if (clientRequireAllResponse) {
                JsonRPCResponse res = (JsonRPCResponse) response.body();

                if (res.getError() != null) {
                    int errorCode = res.getError().getCode();
                    if ((errorCode <= -32000 && errorCode >= -32099) || errorCode == -32603)
                        callback.error(this, res.getError());
                    else
                        callback.unexpectedError(this, new JsonRPCException(res.getError()));
                } else {
                    callback.success(this, response.body());
                }

            } else {
                JsonRPCResponse<T> res = (JsonRPCResponse<T>) response.body();
                if (res.getError() != null) {
                    int errorCode = res.getError().getCode();
                    if ((errorCode <= -32000 && errorCode >= -32099) || errorCode == -32603)
                        callback.error(this, res.getError());
                    else
                        callback.unexpectedError(this, new JsonRPCException(res.getError()));
                } else {
                    callback.success(this, res.getResult());
                }
            }

        } else {
            callback.unexpectedError(this, new JsonRPCException("Unexpected response " + response, JsonRPCError.ERROR_CODE_NETWORK_ERROR));
        }
    }


    @Override
    public void cancel() {
        call.cancel();
    }

    @Override
    public void enqueue(final JsonRPCCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(final Call<T> call, final Response<T> response) {
                if (callbackExecutor == null) {
                    _onResponse(call, response, callback);
                } else {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            _onResponse(call, response, callback);
                        }
                    });
                }
            }

            @Override
            public void onFailure(final Call<T> call, final Throwable t) {
                if (callbackExecutor == null) {
                    callback.unexpectedError(JsonRPCCallAdapter.this, new JsonRPCException(t, JsonRPCError.ERROR_CODE_NETWORK_ERROR));
                } else {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.unexpectedError(JsonRPCCallAdapter.this, new JsonRPCException(t, JsonRPCError.ERROR_CODE_NETWORK_ERROR));
                        }
                    });
                }
            }
        });
    }

    @Override
    public JsonRPCCall<T> clone() {
        return new JsonRPCCallAdapter<T>(call.clone(), callbackExecutor, clientRequireAllResponse);
    }

    @Override
    public T execute() throws JsonRPCException {
        try {
            if (clientRequireAllResponse) {
                T _body = call.execute().body();
                JsonRPCResponse res = (JsonRPCResponse) _body;
                if (res.getError() != null) {
                    throw new JsonRPCException(res.getError());
                }

                return _body;
            } else {
                JsonRPCResponse<T> res = (JsonRPCResponse<T>) call.execute().body();
                if (res.getError() != null) {
                    throw new JsonRPCException(res.getError());
                }
                return res.getResult();
            }
        } catch (IOException e) {
            throw new JsonRPCException(e.getLocalizedMessage(), JsonRPCError.ERROR_CODE_NETWORK_ERROR);
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
