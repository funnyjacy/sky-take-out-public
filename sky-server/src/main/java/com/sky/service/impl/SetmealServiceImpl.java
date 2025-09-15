package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 添加套餐
     * @param setmealDTO
     */
    @Override
    public void insert(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        // 将DTO转换为一个套餐表，一个套餐-菜品关联表
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 提取出套餐-菜品关联集合
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 没有就抛出全局异常
        if (setmealDishes == null || setmealDishes.isEmpty()){
            throw new SetmealEnableFailedException(MessageConstant.DISH_NOT_FOUND);
        }

        // 进行套餐和包含菜品插入操作
        setmealMapper.insert(setmeal);
        // 返回套餐主键，给套餐包含的菜品加上关联套餐标记setmeal_id
        Long setmealId = setmeal.getId();

        // 遍历写入setmeal_id
        setmealDishes.forEach(setmealDish->{
            setmealDish.setSetmealId(setmealId);
        });

        // 向包含菜品表
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 批量删除菜单
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        // 删除菜单 -只能删除停用的套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        // 删除套餐
        setmealMapper.delete(ids);
        // 删除套餐关联菜品表
        setmealDishMapper.delete(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        setmealMapper.update(setmeal);
    }

    @Override
    public SetmealVO queryById(Long id) {
        // 根据id查询套餐
        SetmealVO setmealVO = setmealMapper.queryById(id);
        // 根据id查询关联菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishesById(id);

        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.update(setmeal);
    }

    /**
     * (user)条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * (user)套餐分页查询
     * @param categoryPageQueryDTO
     */
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(categoryPageQueryDTO);

        Long total = page.getTotal();
        List<SetmealVO> records = page.getResult();
        return new PageResult(total, records);

    }
}
