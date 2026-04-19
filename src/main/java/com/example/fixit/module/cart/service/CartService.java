package com.example.fixit.module.cart.service;

import com.example.fixit.module.cart.dto.AddToCartRequestDTO;
import com.example.fixit.module.cart.dto.CartResponseDTO;
import com.example.fixit.module.cart.dto.UpdateCartItemRequestDTO;

public interface CartService {

    CartResponseDTO getMyCart();

    CartResponseDTO addItem(AddToCartRequestDTO request);

    CartResponseDTO updateItem(Long itemId, UpdateCartItemRequestDTO request);

    CartResponseDTO removeItem(Long itemId);

    CartResponseDTO clear();
}
