package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryPageQueryDTO implements Serializable {
    // 套餐分类id
    private Integer categoryId;

    //页码
    private int page;

    //每页记录数
    private int pageSize;

    //分类名称
    private String name;

    //分类类型 1菜品分类  2套餐分类
    private Integer type;

    // 状态 1可用 0禁用 (Constant)
    private Integer status;

}
