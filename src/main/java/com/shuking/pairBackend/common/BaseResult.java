package com.shuking.pairBackend.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "统一响应体") //  定义接口使用到的model的信息
public class BaseResult<T> {

    @Schema(description = "响应编码", example = "200")  //  定义接口使用到的model属性的信息
    private Integer code; //编码

    @Schema(description = "响应信息", example = "处理成功")
    private String msg; //响应信息

    @Schema(description = "详细描述", example = "")
    private String description; //详细描述

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "其他数据")
    private Map<String, Object> map = new HashMap<>(); //动态数据

    public static <T> BaseResult<T> success(String msg,T data) {
        return new BaseResult<>(200, msg, "", data, new HashMap<>());
    }

    public static <T> BaseResult<T> success(String msg) {
        return new BaseResult<T>(200, msg, "",null,  new HashMap<>());
    }

    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<T>(200, "success", "",data,  new HashMap<>());
    }

    public static <T> BaseResult<T> error(String msg, Integer code, String description) {
        return new BaseResult<T>(code, msg, description,null,  new HashMap<>());
    }

    public static <T> BaseResult<T> error() {
        return new BaseResult<T>(500, "error","", null, new HashMap<>());
    }

    public BaseResult<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
