package com.example.fixit.module.product.image.entity;

import com.example.fixit.common.entity.BaseEntity;
import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_images_primary", columnList = "product_id, is_primary")
        }
)
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_images_product"))
    private Product product;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // S3 key stored separately from the full URL so we can delete from S3 without URL parsing
    @Column(name = "s3_key", nullable = false, length = 300)
    private String s3Key;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ImageContentType contentType;

    public enum ImageContentType {
        IMAGE_JPEG,
        IMAGE_PNG,
        IMAGE_WEBP
    }

}
