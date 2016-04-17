package com.xhsoft.retrofit.jsonrpc;

/**
 * JsonRPC 响应
 * @param <T>
 */
public class JsonRPCResponse<T> {
    String jsonrpc;
    long id;
    T result;
    JsonRPCError error;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public long getId() {
        return id;
    }

    public T getResult() {
        return result;
    }

    public JsonRPCError getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRPCResponse<?> that = (JsonRPCResponse<?>) o;

        if (id != that.id) return false;
        //noinspection SimplifiableIfStatement
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        return !(error != null ? !error.equals(that.error) : that.error != null);
    }

    @Override
    public int hashCode() {
        int result1 = (int) (id ^ (id >>> 32));
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (error != null ? error.hashCode() : 0);
        return result1;
    }
}
