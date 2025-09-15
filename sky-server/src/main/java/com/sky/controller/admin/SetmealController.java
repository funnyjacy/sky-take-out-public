package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新添加套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping()
    @ApiOperation("新添加套餐分类")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result add(@RequestBody SetmealDTO setmealDTO){
        log.info("新添加套餐分类：{}", setmealDTO);
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐管理分页查询
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐管理分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("套餐分页查询的条件：{}",categoryPageQueryDTO);
        PageResult pageResult =  setmealService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("需要删除的套餐ids：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 更改套餐状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("更改套餐状态")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("启用禁用套餐id：{},{}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询套餐(数据回显)
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐(数据回显)")
    public Result<SetmealVO> queryById(@PathVariable Long id){
        log.info("查询的套餐id为：{}", id);
        SetmealVO setmealVO = setmealService.queryById(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
