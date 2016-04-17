package com.xhsoft.retrofit.jsonrpc;

import java.util.Random;

/**
 * JsonRPC 请求
 */
public class JsonRPCRequest {
    static final Random RANDOM = new Random();

    final String jsonrpc;
    final String method;
    final Object params;
    final long id;

    JsonRPCRequest(String method, Object params, long id, String jsonrpc) {
        this.method = method;
        this.params = params;
        this.id = id;
        this.jsonrpc = jsonrpc;
    }

    static JsonRPCRequest create(String method, Object args) {
        long id = Math.abs(RANDOM.nextInt());
        return new JsonRPCRequest(method, args, id, "2.0");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRPCRequest that = (JsonRPCRequest) o;

        if (id != that.id) return false;
        //noinspection SimplifiableIfStatement
        if (!method.equals(that.method)) return false;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + params.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}
