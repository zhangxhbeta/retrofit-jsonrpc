package com.segment.jsonrpc;

/**
 * Created by zhangxh on 16/4/13.
 */
public class JsonRPCError {
    Integer code;
    String message;
    Object data;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRPCError that = (JsonRPCError) o;

        if (!code.equals(that.code)) return false;
        return message.equals(that.message);

    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "JsonRPCError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
