package com.zk.projectboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 文件未找到异常类
 * 用于处理请求的文件不存在的情况
 * 被@ResponseStatus注解标记，抛出时会自动转换为HTTP 404响应
 * 继承自RuntimeException，属于非受检异常
 */
@ResponseStatus(HttpStatus.NOT_FOUND)  // 指定异常对应的HTTP状态码为404
public class FileNotFoundException extends RuntimeException {

    /**
     * 带有错误信息的构造函数
     *
     * @param message 错误描述信息，通常包含找不到的文件路径
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * 带有错误信息和原因的构造函数
     *
     * @param message 错误描述信息
     * @param cause 导致异常的原因（通常是另一个异常）
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
