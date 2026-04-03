package com.example.fixit.module.catalog.mapper;

import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.brake.entity.BrakeType;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.product.image.entity.ProductImage;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CatalogMapperTest {

    @Autowired
    private CatalogMapper catalogMapper;

    private Store store;
    private Brake brake;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(1L);
        store.setName("Test Store");
        store.setCity("Achrafieh");
        store.setRating(BigDecimal.valueOf(4.5));
        store.setStatus(StoreStatus.ACTIVE);

        brake = new Brake();
        brake.setId(10L);
        brake.setName("Brembo Brake");
        brake.setBrand("Brembo");
        brake.setPrice(BigDecimal.valueOf(85));
        brake.setStock(10L);
        brake.setStatus(ProductStatus.ACTIVE);
        brake.setCondition(ProductCondition.NEW);
        brake.setBrakeType(BrakeType.DISC);
        brake.setStore(store);
    }

    @Test
    void mapsBasicFields() {
        CatalogResponseDTO dto = catalogMapper.toDTO(brake, "local");

        assertEquals(10L, dto.productId());
        assertEquals("Brembo Brake", dto.name());
        assertEquals("Brembo", dto.brand());
        assertEquals(BigDecimal.valueOf(85), dto.price());
        assertEquals(10L, dto.stock());
    }

    @Test
    void mapsNestedStoreFields() {
        CatalogResponseDTO dto = catalogMapper.toDTO(brake, "local");

        assertEquals("Test Store", dto.storeName());
        assertEquals("Achrafieh", dto.storeCity());
        assertEquals(BigDecimal.valueOf(4.5), dto.storeRating());
    }

    @Test
    void mapsSourceContext() {
        CatalogResponseDTO localDTO = catalogMapper.toDTO(brake, "local");
        assertEquals("local", localDTO.source());

        CatalogResponseDTO remoteDTO = catalogMapper.toDTO(brake, "remote");
        assertEquals("remote", remoteDTO.source());
    }

    @Test
    void nullSource_mapsToNull() {
        CatalogResponseDTO dto = catalogMapper.toDTO(brake, null);
        assertNull(dto.source());
    }

    @Test
    void productWithPrimaryImage_mapsUrl() {
        ProductImage img = new ProductImage();
        img.setUrl("https://cdn.example.com/brake.jpg");
        img.setPrimary(true);
        img.setDisplayOrder(0);
        img.setProduct(brake);
        img.setS3Key("images/brake.jpg");
        img.setFileName("brake.jpg");
        img.setFileSizeBytes(50000L);
        img.setContentType(ProductImage.ImageContentType.IMAGE_JPEG);
        brake.getImages().add(img);

        CatalogResponseDTO dto = catalogMapper.toDTO(brake, "local");

        assertEquals("https://cdn.example.com/brake.jpg", dto.primaryImageUrl());
    }

    @Test
    void productWithoutPrimaryImage_mapsToNull() {
        CatalogResponseDTO dto = catalogMapper.toDTO(brake, "local");

        assertNull(dto.primaryImageUrl());
    }

    @Test
    void productWithNonPrimaryImageOnly_mapsToNull() {
        ProductImage img = new ProductImage();
        img.setUrl("https://cdn.example.com/secondary.jpg");
        img.setPrimary(false);
        img.setDisplayOrder(1);
        img.setProduct(brake);
        img.setS3Key("images/secondary.jpg");
        img.setFileName("secondary.jpg");
        img.setFileSizeBytes(30000L);
        img.setContentType(ProductImage.ImageContentType.IMAGE_PNG);
        brake.getImages().add(img);

        CatalogResponseDTO dto = catalogMapper.toDTO(brake, "local");

        assertNull(dto.primaryImageUrl());
    }
}
