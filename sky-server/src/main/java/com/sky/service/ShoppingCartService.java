package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.vo.OrderSubmitVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void clean();

//    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
