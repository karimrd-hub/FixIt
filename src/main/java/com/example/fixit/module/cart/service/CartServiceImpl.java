package com.example.fixit.module.cart.service;

import com.example.fixit.common.exception.BadRequestException;
import com.example.fixit.module.cart.dto.AddToCartRequestDTO;
import com.example.fixit.module.cart.dto.CartResponseDTO;
import com.example.fixit.module.cart.dto.UpdateCartItemRequestDTO;
import com.example.fixit.module.cart.entity.Cart;
import com.example.fixit.module.cart.entity.CartItem;
import com.example.fixit.module.cart.entity.CartStatus;
import com.example.fixit.module.cart.exception.CartItemNotFoundException;
import com.example.fixit.module.cart.exception.InsufficientStockException;
import com.example.fixit.module.cart.mapper.CartMapper;
import com.example.fixit.module.cart.repository.CartRepository;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.repository.ProductRepository;
import com.example.fixit.module.user.entity.User;
import com.example.fixit.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponseDTO getMyCart() {
        User user = currentUser();
        return cartRepository.findByUserId(user.getId())
                .map(cartMapper::toDTO)
                .orElseGet(() -> new CartResponseDTO(null, CartStatus.ACTIVE, List.of(), BigDecimal.ZERO));
    }

    @Override
    @Transactional
    public CartResponseDTO addItem(AddToCartRequestDTO request) {
        User user = currentUser();
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BadRequestException("Product not found: " + request.productId()));

        Cart cart = getOrCreateActiveCart(user);

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        int newQuantity = existing.map(CartItem::getQuantity).orElse(0) + request.quantity();
        validateStock(product, newQuantity);

        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
            log.info("Incremented cart item {} to quantity={}", existing.get().getId(), newQuantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.quantity());
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
            log.info("Added product={} qty={} to cart={}", product.getId(), request.quantity(), cart.getId());
        }

        Cart saved = cartRepository.save(cart);
        return cartMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CartResponseDTO updateItem(Long itemId, UpdateCartItemRequestDTO request) {
        Cart cart = currentUserCart(itemId);
        CartItem item = findItem(cart, itemId);

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());

        log.info("Updated cart item {} to quantity={}", itemId, request.quantity());
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO removeItem(Long itemId) {
        Cart cart = currentUserCart(itemId);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new CartItemNotFoundException("Cart item not found: " + itemId);
        }
        log.info("Removed cart item {} from cart {}", itemId, cart.getId());
        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO clear() {
        User user = currentUser();
        Optional<Cart> maybeCart = cartRepository.findByUserId(user.getId());
        if (maybeCart.isEmpty()) {
            return new CartResponseDTO(null, CartStatus.ACTIVE, List.of(), BigDecimal.ZERO);
        }
        Cart cart = maybeCart.get();
        cart.getItems().clear();
        log.info("Cleared cart {}", cart.getId());
        return cartMapper.toDTO(cart);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated JWT in security context");
        }
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalStateException("No local user for keycloakId=" + keycloakId));
    }

    private Cart getOrCreateActiveCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    c.setStatus(CartStatus.ACTIVE);
                    Cart saved = cartRepository.save(c);
                    log.info("Created new active cart for user {}", user.getId());
                    return saved;
                });
    }

    // Loads the caller's cart; 404 if they have no cart, which implies the itemId can't exist for them either.
    private Cart currentUserCart(Long itemId) {
        User user = currentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
    }

    private void validateStock(Product product, int requestedQuantity) {
        Long stock = product.getStock();
        if (stock == null || stock < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getId()
                            + ": requested=" + requestedQuantity
                            + ", available=" + (stock == null ? 0 : stock));
        }
    }
}
