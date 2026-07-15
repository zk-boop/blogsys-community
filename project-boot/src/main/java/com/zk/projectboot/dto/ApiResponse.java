package com.zk.projectboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API通用响应对象
 * 用于统一后端API的响应格式
 * 包含请求处理状态、消息和数据载荷
 * @param <T> 响应数据的类型，支持泛型
 */
@Data                // Lombok注解，自动生成getter、setter、equals、hashCode和toString方法
@NoArgsConstructor   // Lombok注解，生成无参构造函数
@AllArgsConstructor  // Lombok注解，生成包含所有字段的构造函数
public class ApiResponse<T> {

    /**
     * 请求处理状态
     * true表示成功，false表示失败
     */
    private boolean success;

    /**
     * 响应消息
     * 成功或失败的描述信息
     */
    private String message;

    /**
     * 响应数据载荷
     * 可以是任何类型的数据对象
     */
    private T data;

    /**
     * 创建成功响应，使用默认成功消息
     * @param <T> 响应数据类型
     * @param data 响应数据
     * @return 成功的API响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    /**
     * 创建成功响应，使用自定义成功消息
     * @param <T> 响应数据类型
     * @param message 自定义成功消息
     * @param data 响应数据
     * @return 成功的API响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 创建失败响应，不包含数据
     * @param <T> 响应数据类型
     * @param message 失败原因描述
     * @return 失败的API响应对象
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * 创建失败响应，包含错误相关数据
     * @param <T> 响应数据类型
     * @param message 失败原因描述
     * @param data 错误相关数据
     * @return 失败的API响应对象
     */
    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
