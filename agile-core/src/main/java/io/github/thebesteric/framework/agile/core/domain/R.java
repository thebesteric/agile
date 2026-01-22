package io.github.thebesteric.framework.agile.core.domain;

import io.github.thebesteric.framework.agile.commons.util.TransactionUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.beans.Transient;
import java.util.Optional;

@Data
public class R<T> {

    private int successCode = HttpStatus.OK.value();
    private int errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private boolean succeed = false;

    private Integer code;
    private String message;
    private T data;
    private HttpStatus httpStatus;
    private String trackId;

    private R() {
    }

    public static <T> R<T> build(int code, String message, T body, boolean isSuccess, HttpStatus httpStatus) {
        R<T> result = new R<>();
        // Set Attributes
        result.setCode(code);
        result.setMessage(message);
        result.setTrackId(TransactionUtils.get());
        // Set data
        if (body != null) {
            result.setData(body);
        }
        // Set success or error code and succeed flag
        if (isSuccess) {
            result.setSuccessCode(code);
            result.setSucceed(true);
        } else {
            result.setErrorCode(code);
            result.setSucceed(false);
        }
        // If httpStatus is null, then set it to the status of the current request
        if (httpStatus == null) {
            HttpServletResponse httpServletResponse = result.getHttpServletResponse();
            if (httpServletResponse != null) {
                result.setHttpStatus(httpServletResponse.getStatus());
            } else {
                result.setHttpStatus(HttpStatus.OK);
            }
        } else {
            result.setHttpStatus(httpStatus);
        }
        // Return result
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
        return build(code, message, data, true, null);
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
        return build(code, message, data, false, null);
    }

    public static <T> T extractData(R<T> result, T defaultValue) {
        return Optional.ofNullable(result).filter(R::isSucceed).map(R::getData).orElse(defaultValue);
    }

    public T extractData(T defaultValue) {
        return extractData(this, defaultValue);
    }

    public R<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    public R<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    public R<T> data(T data) {
        this.setData(data);
        return this;
    }

    public R<T> httpStatus(HttpStatus httpStatus) {
        this.setHttpStatus(httpStatus);
        return this;
    }

    public R<T> httpStatus(int httpStatusCode) {
        setHttpStatus(httpStatusCode);
        return this;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        setHttpServletResponseStatus(httpStatus);
    }

    public void setHttpStatus(int httpStatusCode) {
        this.httpStatus = HttpStatus.valueOf(httpStatusCode);
        setHttpStatus(this.httpStatus);
    }

    private HttpServletResponse getHttpServletResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getResponse();
        }
        return null;
    }

    private void setHttpServletResponseStatus(HttpStatus httpStatus) {
        HttpServletResponse httpServletResponse = getHttpServletResponse();
        if (httpServletResponse != null) {
            httpServletResponse.setStatus(httpStatus.value());
        }
    }

    @Transient
    public int getSuccessCode() {
        return this.successCode;
    }

    @Transient
    public int getErrorCode() {
        return this.errorCode;
    }
}