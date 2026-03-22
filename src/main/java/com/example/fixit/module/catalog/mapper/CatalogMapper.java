package com.example.fixit.module.catalog.mapper;

import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.image.entity.ProductImage;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    /*
    - MapStruct auto-maps name, brand, price, stock since they match exactly between Product and the DTO
    - @Mapping annotations handle the nested fields (store.name → storeName, etc.)
    - primaryImageUrl uses an expression since it needs the null-check logic — defined as a default method on the interface
    - source uses @Context — MapStruct passes it through without trying to map it from the entity, and the expression injects it into the DTO
    */
   
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.store.name", target = "storeName")
    @Mapping(source = "product.store.city", target = "storeCity")
    @Mapping(source = "product.store.rating", target = "storeRating")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImageUrl(product))")
    @Mapping(target = "source", expression = "java(source)")
    CatalogResponseDTO toDTO(Product product, @Context String source);

    default String resolvePrimaryImageUrl(Product product) {
        ProductImage primary = product.getPrimaryImage();
        return primary != null ? primary.getUrl() : null;
    }
}
