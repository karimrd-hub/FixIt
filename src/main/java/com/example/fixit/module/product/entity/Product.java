package com.example.fixit.module.product.entity;

import com.example.fixit.common.entity.AuditableEntity;
import com.example.fixit.module.product.image.entity.ProductImage;
import com.example.fixit.module.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"store", "images"})
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "product_type",
        discriminatorType = DiscriminatorType.STRING
)
public class Product extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Long stock = 0L;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false, length = 10)
    private ProductCondition condition = ProductCondition.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = true, foreignKey = @ForeignKey(name = "fk_products_store"))
    private Store store;

    // CascadeType.ALL + orphanRemoval ensures images are deleted when the product is deleted
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    // Convenience method — avoids loading the full images collection just to get the primary
    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .orElse(null);
    }
}
