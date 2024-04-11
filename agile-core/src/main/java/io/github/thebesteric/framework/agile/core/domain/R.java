package io.github.thebesteric.framework.agile.core.domain;

import io.github.thebesteric.framework.agile.commons.util.TransactionUtils;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class R<T> {

    private Integer code;
    private String message;
    private T data;
    private String trackId;

    private R() {
    }

    public static <T> R<T> build(int code, String message, T body) {
        R<T> result = new R<>();
        if (body != null) {
            result.setData(body);
        }
        result.setCode(code);
        result.setMessage(message);
        result.setTrackId(TransactionUtils.get());
        return result;
    }


    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> success(T data) {
        return success(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> R<T> success(HttpStatus httpStatus, String message) {
        return success(httpStatus.value(), message, null);
    }

    public static <T> R<T> success(int code, String message) {
        return success(code, message, null);
    }

    public static <T> R<T> success(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> R<T> success(HttpStatus httpStatus, String message, T data) {
        return success(httpStatus.value(), message, data);
    }

    public static <T> R<T> success(int code, String message, T data) {
        return build(code, message, data);
    }

    public static <T> R<T> error() {
        return error(null);
    }

    public static <T> R<T> error(T data) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), data);
    }

    public static <T> R<T> error(HttpStatus httpStatus, String message) {
        return error(httpStatus.value(), message, null);
    }

    public static <T> R<T> error(int code, String message) {
        return error(code, message, null);
    }

    public static <T> R<T> error(String message, T data) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, data);
    }

    public static <T> R<T> error(HttpStatus httpStatus, String message, T data) {
        return error(httpStatus.value(), message, data);
    }

    public static <T> R<T> error(int code, String message, T data) {
        return build(code, message, data);
    }

    public R<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    public R<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    public R<T> data(T data) {
        this.setData(data);
        return this;
    }
}