package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 插入口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除对应的口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * (更新)批量删除口味
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新口味
     * @param dishFlavor
     */
    void update(DishFlavor dishFlavor);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteAll(Long dishId);
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> queryById(Long dishId);
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long id);
}
