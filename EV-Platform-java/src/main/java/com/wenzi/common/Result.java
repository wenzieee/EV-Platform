package com.wenzi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一返回结果类
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码：200 代表成功，500 代表后端内部错误，401 代表未登录等
     */
    private Integer code;

    /**
     * 提示信息：例如 "操作成功"、"账号密码错误" 等
     */
    private String msg;

    /**
     * 核心数据包：泛型 T 代表可以装入任何类型的数据（如 User 对象、List 集合等）
     */
    private T data;

    // 私有化构造方法，强制规范大家使用下面提供的静态方法
    private Result() {}

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ================= 成功响应的方法 =================

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // ================= 失败响应的方法 =================

    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败，系统异常", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}