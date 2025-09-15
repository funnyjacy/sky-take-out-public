package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setMealDishMapper;
    /**
     * 保存菜品操作（口味可选）
     * @param dishDTO
     */
    @Override
    @Transactional // 事件
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish(); // 有口味和无口味区分

        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入一条数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        // (如果有)向口味表插入一条数据
        if (flavors != null && !flavors.isEmpty()){
            // 遍历写入dish_id
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
//        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
//        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
//        long total = page.getTotal();
//        List<Employee> records = page.getResult();
//        return new PageResult(total, records);
        // 利用PageHelper设置查询页面号，页面显示数量
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        // PageHelper规定会自动在持久层的page方法返回集合
        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> records = page.getResult();

        return new PageResult(total, records);

    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        // 判断菜品是否生效
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                // 菜品处于在售状态，不能删除，异常会返回给前端解决
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断菜品是否关联套餐
        List<Long> setMealIds = setMealDishMapper.getSetMealIdsByDishIds(ids);
        if (setMealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表中的菜品数据
//        for (Long id : ids) {
//            dishMapper.delete(id);
//        }
        // 单个删除调用过多sql语句，优化为使用批量删除语句
        dishMapper.deleteBatch(ids);

        // 删除口味表中关联的口味 --直接根据菜品id删除
//        for (Long id : ids) {
//            dishFlavorMapper.deleteByDishId(id);
//        }
        // 单个删除调用过多sql语句，优化为批量删除
        dishFlavorMapper.deleteBatch(ids);

    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void update(DishDTO dishDTO) {
        // 修改之前是会有查询操作，因此这个dishDTO应该是有id的
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 更新菜品信息
        dishMapper.update(dish);

        // 两种情况 1.原来口味修改 2.新增口味
        // 一种是删除原来的统一重新写入
        // 另一种是修改原来的，新增口味另外写
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 先把原来的删除干净，后面一起写入
        dishFlavorMapper.deleteAll(dishId);
        if (flavors != null && !flavors.isEmpty()){
            // 遍历写入dish_id
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据id查询菜品(菜品 + 口味)
     * @param id
     * @return
     */
    @Override
    public DishVO queryById(Long id) {
        // 在菜品表中根据id返回菜品信息
        DishVO dishVO = dishMapper.queryById(id);
        // 根据id在口味表中返回口味集合
        List<DishFlavor> flavors = dishFlavorMapper.queryById(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * (user商品浏览)条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询对应菜品列表(全部)
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> listByCategoryId(Integer categoryId) {
        List<Dish> dished = dishMapper.listByCategoryId(categoryId);
        return dished;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

}
