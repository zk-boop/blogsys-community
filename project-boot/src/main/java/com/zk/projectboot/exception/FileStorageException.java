package com.zk.projectboot.exception;

/**
 * 文件存储异常类
 * 用于处理文件存储过程中出现的各种错误情况
 * 例如文件创建失败、目录创建失败、权限不足等
 * 继承自RuntimeException，属于非受检异常
 */
public class FileStorageException extends RuntimeException {

    /**
     * 带有错误信息的构造函数
     *
     * @param message 错误描述信息
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * 带有错误信息和原因的构造函数
     *
     * @param message 错误描述信息
     * @param cause 导致异常的原因（通常是另一个异常）
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
