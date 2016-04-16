package com.xhsoft.retrofit.jsonrpc;

class JsonRPCNotification {

    final String jsonrpc;
    final String method;
    final Object params;

    JsonRPCNotification(String method, Object params, String jsonrpc) {
        this.method = method;
        this.params = params;
        this.jsonrpc = jsonrpc;
    }

    static JsonRPCNotification create(String method, Object args) {
        return new JsonRPCNotification(method, args, "2.0");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRPCNotification that = (JsonRPCNotification) o;

        //noinspection SimplifiableIfStatement
        if (!method.equals(that.method)) return false;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + params.hashCode();
        return result;
    }
}
